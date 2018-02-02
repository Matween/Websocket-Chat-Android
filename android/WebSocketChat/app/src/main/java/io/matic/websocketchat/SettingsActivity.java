package io.matic.websocketchat;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

public class SettingsActivity extends AppCompatActivity {

    ImageButton buttonInfo;
    Button buttonChangePassword;
    EditText editTextOldPassword;
    EditText editTextNewPassword;
    EditText editTextPasswordRepeat;
    private static SettingsActivity instance;
    private ProgressBar progressBar;
    ImageView imageViewProfilePicBoy;
    ImageView imageViewProfilePicGirl;
    ImageView imageViewProfilePicMan;
    ImageView imageViewProfilePicWoman;
    private static final String selectedColor = "#FF1FD428";
    private final Context context = this;

    public static SettingsActivity getInstance() {
        return instance;
    }

    public static void setInstance(SettingsActivity instance) {
        SettingsActivity.instance = instance;
    }

    public ProgressBar getProgressBar() {
        return progressBar;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        // save instance
        setInstance(this);
        imageViewProfilePicBoy = (ImageView) findViewById(R.id.imageViewProfilePicBoy);
        imageViewProfilePicGirl = (ImageView) findViewById(R.id.imageViewProfilePicGirl);
        imageViewProfilePicMan = (ImageView) findViewById(R.id.imageViewProfilePicMan);
        imageViewProfilePicWoman = (ImageView) findViewById(R.id.imageViewProfilePicWoman);
        // set selected image according to users image
        setSelectedImage(MainActivity.getCurrentUser().getProfilePic());

        /*
        * send image info depending on which picture was chosen
        * */
        imageViewProfilePicBoy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendImageInfo("image_boy");
            }
        });
        imageViewProfilePicGirl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendImageInfo("image_girl");
            }
        });
        imageViewProfilePicMan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendImageInfo("image_man");
            }
        });
        imageViewProfilePicWoman.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendImageInfo("image_woman");
            }
        });
        buttonInfo = (ImageButton) findViewById(R.id.buttonInfo);
        // show info dialog
        buttonInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Dialog dialog = new Dialog(context);
                dialog.setContentView(R.layout.info_dialog);
                dialog.show();
            }
        });
        editTextOldPassword = (EditText) findViewById(R.id.editTextPasswordOld);
        editTextNewPassword = (EditText) findViewById(R.id.editTextPasswordNew);
        editTextPasswordRepeat = (EditText) findViewById(R.id.editTextPasswordNewRep);
        buttonChangePassword = (Button) findViewById(R.id.buttonChangePassword);
        buttonChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // show progress bar
                progressBar = (ProgressBar) findViewById(R.id.progressBarSettings);
                progressBar.setVisibility(View.VISIBLE);
                JSONObject sendTo = new JSONObject();
                JSONObject user = new JSONObject();
                try {
                    sendTo.put("flag", "changePassword");
                    user.put("id", MainActivity.getCurrentUser().getUserID());
                    user.put("fullname", MainActivity.getCurrentUser().getFullName());
                    user.put("email", MainActivity.getCurrentUser().getEmail());
                    // put old password, hash and salt it
                    user.put("oldPassword", StaticVariables.bytesToHex(editTextOldPassword.getText().toString()));
                    if(editTextNewPassword.getText().toString()
                            .equals(editTextPasswordRepeat.getText().toString())) {
                        // if password fields match
                        // put new password, hash it and salt it
                        user.put("newPassword", StaticVariables.bytesToHex(editTextNewPassword.getText().toString()));
                    } else {
                        // if they don't notify user and hide progressbar
                        getProgressBar().setVisibility(View.INVISIBLE);
                        Toast.makeText(getApplicationContext(), "Passwords do not match!", Toast.LENGTH_SHORT).show();
                        // enable text boxes and buttons
                        enableControls();
                    }
                    sendTo.put("user", user);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if(WebSocketChatClient.getInstance() != null) {
                    // hide text fields and buttons
                    disableControls();
                    // send password change request
                    WebSocketChatClient.getInstance().send(sendTo.toString());
                    // wait for websocket response
                }
            }
        });
    }

    // send request to change image to websocket and in toolbar
    public void sendImageInfo(String imageInfo) {
        if(WebSocketChatClient.getInstance() != null) {
            // prepare json object with proper flag
            JSONObject send = new JSONObject();
            JSONObject user = new JSONObject();
            try {
                send.put("flag", "newImage");
                send.put("image", imageInfo);
                user.put("id", MainActivity.getCurrentUser().getUserID());
                user.put("fullname", MainActivity.getCurrentUser().getFullName());
                user.put("email", MainActivity.getCurrentUser().getEmail());
                send.put("userFrom", user);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            // send to websocket
            WebSocketChatClient.getInstance().send(send.toString());
            // change profile pic
            MainActivity.getCurrentUser().setProfilePic(imageInfo);
            MainActivity.getToolbar().setNavigationIcon(StaticVariables.getProfilePic(imageInfo));
            // deselect all images
            unsetsetSelectedImage();
            // set selected
            setSelectedImage(imageInfo);
        }
    }

    // sets the selected image
    public void setSelectedImage(String image) {
        if(image.equals("image_boy")) {
            imageViewProfilePicBoy.setBackgroundColor(Color.parseColor(selectedColor));
        } else if(image.equals("image_girl")) {
            imageViewProfilePicGirl.setBackgroundColor(Color.parseColor(selectedColor));
        } else if(image.equals("image_man")) {
            imageViewProfilePicMan.setBackgroundColor(Color.parseColor(selectedColor));
        } else if(image.equals("image_woman")) {
            imageViewProfilePicWoman.setBackgroundColor(Color.parseColor(selectedColor));
        }
    }

    // removes background from all images
    public void unsetsetSelectedImage() {
        imageViewProfilePicBoy.setBackgroundColor(Color.parseColor("#00FFFFFF"));
        imageViewProfilePicGirl.setBackgroundColor(Color.parseColor("#00FFFFFF"));
        imageViewProfilePicMan.setBackgroundColor(Color.parseColor("#00FFFFFF"));
        imageViewProfilePicWoman.setBackgroundColor(Color.parseColor("#00FFFFFF"));
    }

    // disables clicking on buttons or text boxes -> makes them invisible
    public void disableControls() {
        editTextOldPassword.setVisibility(View.INVISIBLE);
        editTextNewPassword.setVisibility(View.INVISIBLE);
        editTextPasswordRepeat.setVisibility(View.INVISIBLE);
        buttonChangePassword.setVisibility(View.INVISIBLE);
        buttonInfo.setVisibility(View.INVISIBLE);
    }

    // enables text boxes and buttons -> makss them visible
    public void enableControls() {
        editTextOldPassword.setVisibility(View.VISIBLE);
        editTextNewPassword.setVisibility(View.VISIBLE);
        editTextPasswordRepeat.setVisibility(View.VISIBLE);
        buttonChangePassword.setVisibility(View.VISIBLE);
        buttonInfo.setVisibility(View.VISIBLE);
    }
}

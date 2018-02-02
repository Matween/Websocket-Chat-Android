package io.matic.websocketchat;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import org.java_websocket.drafts.Draft_17;


public class LoginActivity extends AppCompatActivity {


    Button buttonLogin;
    Button buttonRegister;
    EditText editTextEmail;
    EditText editTextPassword;
    private static LoginActivity instance;
    private ProgressBar progressBar;

    public ProgressBar getProgressBar() {
        return progressBar;
    }

    public void setProgressBar(ProgressBar progressBar) {
        this.progressBar = progressBar;
    }

    public static LoginActivity getInstance() {
        return instance;
    }

    public static void setInstance(LoginActivity instance) {
        LoginActivity.instance = instance;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setInstance(this);

        editTextEmail = (EditText) findViewById(R.id.editTextEmail);
        editTextPassword = (EditText) findViewById(R.id.editTextPassword);
        buttonLogin = (Button) findViewById(R.id.buttonLogin);
        // send login request
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StaticVariables.setEmail(editTextEmail.getText().toString());
                // hash and salt the password
                StaticVariables.setPassword(StaticVariables.bytesToHex(editTextPassword.getText().toString()));
                // set request URI to connect to websocket
                StaticVariables.setURI("ws://" + StaticVariables.getServerIP() +
                        "/websocketchatapi/chat?login=true&email="
                        + StaticVariables.getEmail()
                        + "&pw=" + StaticVariables.getPassword());
                // connect to websocket
                if(WebSocketChatClient.getInstance() == null) {
                    WebSocketChatClient.setInstance
                            (new WebSocketChatClient(StaticVariables.getURI(), new Draft_17()));
                }
                // set login activity as active
                // notifies if websocket can do something on login activity
                WebSocketChatClient.getInstance().setLogActive(true);
                WebSocketChatClient.getInstance().connect();
                // disable buttons and text boxes
                disableControls();
                // show progress bar
                progressBar = (ProgressBar) findViewById(R.id.progressBarLogin);
                if(progressBar.getVisibility() != View.VISIBLE){ // check if it is visible
                    progressBar.setVisibility(View.VISIBLE); // if not set it to visible
                }
            }
        });
        buttonRegister = (Button) findViewById(R.id.buttonRegister);
        // open register activity
        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

    }

    // disable buttons and text boxes
    public void disableControls() {
        editTextEmail.setVisibility(View.INVISIBLE);
        editTextPassword.setVisibility(View.INVISIBLE);
        buttonRegister.setVisibility(View.INVISIBLE);
        buttonLogin.setVisibility(View.INVISIBLE);
    }

    // enable buttons and text boxes
    public void enableControls() {
        editTextEmail.setVisibility(View.VISIBLE);
        editTextPassword.setVisibility(View.VISIBLE);
        buttonRegister.setVisibility(View.VISIBLE);
        buttonLogin.setVisibility(View.VISIBLE);
    }
}

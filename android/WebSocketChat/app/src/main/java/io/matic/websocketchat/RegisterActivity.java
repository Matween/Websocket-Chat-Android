package io.matic.websocketchat;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import org.java_websocket.drafts.Draft_17;

public class RegisterActivity extends AppCompatActivity {

    Button buttonRegister;
    Button buttonLogin;
    EditText editTextName;
    EditText editTextEmail;
    EditText editTextPassword;
    private ProgressBar progressBar;

    private static RegisterActivity instance;
    public static RegisterActivity getInstance() {
        return instance;
    }
    public static void setInstance(RegisterActivity instance) {
        RegisterActivity.instance = instance;
    }

    public ProgressBar getProgressBar() {
        return progressBar;
    }

    public void setProgressBar(ProgressBar progressBar) {
        this.progressBar = progressBar;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        // save instance
        setInstance(this);
        editTextName = (EditText) findViewById(R.id.editTextName);
        editTextEmail = (EditText) findViewById(R.id.editTextEmail);
        editTextPassword = (EditText) findViewById(R.id.editTextPassword);
        buttonRegister = (Button) findViewById(R.id.buttonRegister);
        // send register request
        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StaticVariables.setEmail(editTextEmail.getText().toString());
                // hash and salt the password
                StaticVariables.setPassword(StaticVariables.bytesToHex(editTextPassword.getText().toString()));
                // set request URI
                StaticVariables.setURI("ws://" + StaticVariables.getServerIP() +
                        "/websocketchatapi/chat?register=true" +
                        "&fullname=" + editTextName.getText().toString().replace(" ", "+") +
                        "&email=" + StaticVariables.getEmail() +
                        "&pw=" + StaticVariables.getPassword());
                // open websocket connection
                if(WebSocketChatClient.getInstance() == null) {
                    WebSocketChatClient.setInstance(
                            new WebSocketChatClient(StaticVariables.getURI(), new Draft_17()));
                }
                WebSocketChatClient.getInstance().setRegActive(true);
                WebSocketChatClient.getInstance().connect();
                // disable buttons and text boxes
                disableControls();
                progressBar = (ProgressBar) findViewById(R.id.progressBarRegister);
                if(progressBar.getVisibility() != View.VISIBLE){ // check if it is visible
                    progressBar.setVisibility(View.VISIBLE); // if not set it to visible
                }
            }
        });
        buttonLogin = (Button) findViewById(R.id.buttonLogin);
        // open login activity
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }

    // disable buttons and text boxes
    public void disableControls() {
        editTextName.setVisibility(View.INVISIBLE);
        editTextEmail.setVisibility(View.INVISIBLE);
        editTextPassword.setVisibility(View.INVISIBLE);
        buttonRegister.setVisibility(View.INVISIBLE);
        buttonLogin.setVisibility(View.INVISIBLE);
    }
    // enable buttons and text boxes
    public void enableControls() {
        editTextName.setVisibility(View.VISIBLE);
        editTextEmail.setVisibility(View.VISIBLE);
        editTextPassword.setVisibility(View.VISIBLE);
        buttonRegister.setVisibility(View.VISIBLE);
        buttonLogin.setVisibility(View.VISIBLE);
    }

}

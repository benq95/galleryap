package android.java.pl.galleryap.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.java.pl.galleryap.R;
import android.java.pl.galleryap.service.HttpResponseListener;
import android.java.pl.galleryap.service.HttpService;
import android.java.pl.galleryap.service.ServiceException;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * Login activity to create new user and log in. It has two modes -> new user and login
 */
public class LoginActivity extends AppCompatActivity implements HttpResponseListener {

    private HttpService httpService;
    private HttpResponseListener selfInstance;

    private TextView passwordText;
    private TextView usernamedText;
    private TextView errorText;
    private TextView newUserText;
    private Button loginButton;
    private EditText editUsername;
    private EditText editPassword;
    private ProgressBar progressBar;

    private boolean loginMode;//activity modes


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        httpService = HttpService.getInstance(this.getApplicationContext());
        selfInstance = this;

        loginButton = (Button)findViewById(R.id.button);
        passwordText = (TextView)findViewById(R.id.textPassword);
        usernamedText = (TextView)findViewById(R.id.textUsername);
        errorText = (TextView)findViewById(R.id.errorText);
        newUserText = (TextView)findViewById(R.id.newUserText);
        editUsername = (EditText)findViewById(R.id.editUsername);
        editPassword = (EditText)(findViewById(R.id.editPassword));
        progressBar = (ProgressBar)findViewById(R.id.progressBar);

        newUserText.setOnClickListener(modeSwitch);

        //set mode to log in
        loginMode = false;
        switchMode();
    }

    //button listener for login mode
    private View.OnClickListener loginButtonOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String username = editUsername.getText().toString();
            String password = editPassword.getText().toString();
            try{
                httpService.login(username,password,selfInstance);
                progressBar.setVisibility(View.VISIBLE);
                progressBar.animate();
                errorText.setVisibility(View.INVISIBLE);
            } catch (ServiceException e){
                errorText.setVisibility(View.VISIBLE);
                errorText.setText(e.getMessage());
            }
            enable(false);
        }
    };

    //button listener for new user mode
    private  View.OnClickListener newUserButtonClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String username = editUsername.getText().toString();
            String password = editPassword.getText().toString();
            try{
                httpService.newUser(username,password,selfInstance);
                progressBar.setVisibility(View.VISIBLE);
                progressBar.animate();
                errorText.setVisibility(View.INVISIBLE);
            } catch (ServiceException e){
                errorText.setVisibility(View.VISIBLE);
                errorText.setText(e.getMessage());
            }
            enable(false);
        }
    };

    //listener for mode switchs
    private View.OnClickListener modeSwitch = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switchMode();
        }
    };

    //error event method
    @Override
    public void GetError(String message) {
        enable(true);
        clear();
        errorText.setText(message);
        errorText.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.INVISIBLE);
    }

    //internet response event method
    @Override
    public void GetResponse() {
        enable(true);
        progressBar.setVisibility(View.INVISIBLE);
        clear();
        if(!loginMode){//if login mode start Main activity
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setCancelable(false);
            dialog.setTitle("Success");
            dialog.setMessage("User successfully created, try to log in" );
            dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    //Action for "Delete".
                }
            });
            final AlertDialog alert = dialog.create();
            alert.show();
            switchMode();
            return;
        }
        Intent intent = new Intent(this,MainActivity.class);
        this.startActivity(intent);
    }

    //change layout to another mode
    private void switchMode(){

        progressBar.setVisibility(View.INVISIBLE);
        if(loginMode==true){
            loginButton.setText("Create user");
            passwordText.setText("Insert new password");
            usernamedText.setText("Insert new username");
            errorText.setVisibility(View.INVISIBLE);
            newUserText.setText("Try to login");
            loginButton.setOnClickListener(newUserButtonClick);
            loginMode = false;
        }else{
            loginButton.setText("Login");
            passwordText.setText("Insert password");
            usernamedText.setText("Insert username");
            errorText.setVisibility(View.INVISIBLE);
            newUserText.setText("Create new user");
            loginButton.setOnClickListener(loginButtonOnClick);
            loginMode = true;
        }
    }

    protected boolean enabled = true;

    //enable or disable touch on activity
    private void enable(boolean b) {
        enabled = b;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return enabled ?
                super.dispatchTouchEvent(ev) :
                true;
    }

    private void clear(){
        editPassword.setText("");
        editUsername.setText("");
    }
}

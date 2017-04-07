package com.example.alanzheng.pocketcleric;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class LoginActivity extends AppCompatActivity {

    // Declare instance variables
    public static final String DEBUG = "LogicApp";
    public static final String PREFS_TAG = "LoginData";
    public static final String DEFAULT = "NULL";
    public static final int CONNECTION_TIMEOUT = 10000; // in milliseconds
    public static final int READ_TIMEOUT = 15000; // in milliseconds

    private EditText etUsername;
    private EditText etPassword;

    private Session session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Get Reference to variables
        etUsername = (EditText) findViewById(R.id.usernameEditText);
        etPassword = (EditText) findViewById(R.id.passwordEditText);

        // User Session Manager
        session = new Session(getApplicationContext());
        //Toast.makeText(getApplicationContext(), "User Login Status: " + session.isUserLoggedIn(), Toast.LENGTH_LONG).show();
    }

    // Triggers when LOGIN Button clicked
    public void submitLogin(View view) {

        // Get text from username and password field
        final String username = etUsername.getText().toString().trim();
        final String password = etPassword.getText().toString();

        boolean usernameEntered = !username.isEmpty();
        boolean passwordEntered = !password.isEmpty();

        // Check if username field is not blank
        if (usernameEntered)
        {
            // Check if password field is not blank
            if (passwordEntered)
            {
                // Initialize  AsyncLogin() class with username and password
                new AsyncLogin().execute(username, password);
            }
            else {
                // Password too short
                Toast.makeText(this, "Please enter your password", Toast.LENGTH_SHORT).show();
            }
        } else {
            // No password supplied by user
            Toast.makeText(this, "Please enter your username", Toast.LENGTH_SHORT).show();
        }
    }

    // Triggers when SIGNUP Button clicked
    public void submitRegister(View view) {
        // Create an intent for signup activity
        Intent i = new Intent(this, SignupActivity.class);
        // Send user to next activity
        startActivity(i);
    }

    // Triggers when GUEST Button clicked
    public void guestLogin(View view) {
        Intent i = new Intent(this, ReceptorActivity.class);
        startActivity(i);
    }

    private class AsyncLogin extends AsyncTask<String, String, String>
    {
        ProgressDialog pdLoading = new ProgressDialog(LoginActivity.this);
        HttpURLConnection conn;
        URL url = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // This method will be running on UI thread
            pdLoading.setMessage("\tLogging In...");
            pdLoading.setCancelable(false);
            pdLoading.show();
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                // Enter URL address where your php file resides

                // Uncomment if developing on local machine (XAMPP)
                //url = new URL("http://10.0.2.2/pocketcleric/login.inc.php");

                // Uncomment if testing on live server
                url = new URL("http://www.prawnskunk.com/pocketcleric/login.inc.php");

            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return "exception";
            }
            try {
                // Setup HttpURLConnection class to send and receive data from php and mysql
                conn = (HttpURLConnection)url.openConnection();
                conn.setReadTimeout(READ_TIMEOUT);
                conn.setConnectTimeout(CONNECTION_TIMEOUT);
                conn.setRequestMethod("POST");

                // setDoInput and setDoOutput method depict handling of both send and receive
                conn.setDoInput(true);
                conn.setDoOutput(true);

                // Append parameters to URL
                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("username", params[0])
                        .appendQueryParameter("password", params[1]);
                String query = builder.build().getEncodedQuery();

                // Open connection for sending data
                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(query);
                writer.flush();
                writer.close();
                os.close();
                conn.connect();

            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
                return "exception";
            }

            try {

                int response_code = conn.getResponseCode();

                // Check if successful connection made
                if (response_code == HttpURLConnection.HTTP_OK) {

                    // Read data sent from server
                    InputStream input = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                    StringBuilder result = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }

                    // Pass data to onPostExecute method
                    return(result.toString());

                }else{

                    return("unsuccessful");
                }

            } catch (IOException e) {
                e.printStackTrace();
                return "exception";
            } finally {
                conn.disconnect();
            }
        }

        @Override
        protected void onPostExecute(String result) {

            //this method will be running on UI thread

            pdLoading.dismiss();

            if(result.equalsIgnoreCase("true")) {
                // Get text from username and password field
                final String username = etUsername.getText().toString().trim();
                final String password = etPassword.getText().toString();
                // Create new user login session
                session.createUserLoginSession(username, password);

                /* Here launching another activity when login successful. If you persist login state
                use sharedPreferences of Android. and logout button to clear sharedPreferences.
                 */
                Intent intent = new Intent(LoginActivity.this,TethererActivity.class);

                startActivity(intent);
                LoginActivity.this.finish();
            } else if (result.equalsIgnoreCase("false")) {
                // If username and password does not match display a error message
                Toast.makeText(LoginActivity.this, "Incorrect username or password", Toast.LENGTH_LONG).show();
            } else if (result.equalsIgnoreCase("exception") || result.equalsIgnoreCase("unsuccessful")) {

                Toast.makeText(LoginActivity.this, "OOPs! Something went wrong. Connection Problem.", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onBackPressed()
    {
        // Exit application immediately
        //moveTaskToBack(true);

        // Go to main activity
        Intent i = new Intent(this, MainActivity.class);
        // Send user to next activity
        startActivity(i);
    }
}

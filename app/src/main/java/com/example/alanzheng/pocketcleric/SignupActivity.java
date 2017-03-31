package com.example.alanzheng.pocketcleric;

import android.app.ProgressDialog;
import android.content.Context;
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

public class SignupActivity extends AppCompatActivity {

    // Declare instance variables
    public EditText usernameEditText;
    public EditText passwordEditText;
    public EditText password2EditText;
    public static final String PREFS_TAG = "LoginData";
    public static final String DEFAULT = "NULL";
    public static final int CONNECTION_TIMEOUT = 10000; // in milliseconds
    public static final int READ_TIMEOUT = 15000; // in milliseconds

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Initialize instance variables
        usernameEditText = (EditText) findViewById(R.id.usernameEditText);
        passwordEditText = (EditText) findViewById(R.id.passwordEditText);
        password2EditText = (EditText) findViewById(R.id.password2EditText);
    }

    //  @+id/submitButton android:onClick hook
    public void submitRegistration(View view)
    {
        // True/false values for logic control
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString();
        String password2 = password2EditText.getText().toString();
        boolean usernameEntered = !username.equals("");
        boolean passwordEntered = !password.equals("");
        boolean passwordsMatch = password.equals(password2);

        // Preferences files identified by the first parameter
        SharedPreferences sp = getSharedPreferences(PREFS_TAG, Context.MODE_PRIVATE);

        // Call edit() to get a SharedPreferences.Editor
        SharedPreferences.Editor e = sp.edit();

        // Check if username field is not blank
        if (usernameEntered)
        {
            // Check if password field is not blank
            if (passwordEntered)
            {
                // Check password length (too short)
                if (password.length() > 5)
                {
                    // Check password length (too long)
                    if (password.length() < 60)
                    {
                        // Check if passwords match
                        if (passwordsMatch)
                        {
                            // Check password does not equal username
                            if (!password.equals(username))
                            {
                                // Add values with methods such as putBoolean() and putString().
                                e.putString("username_" + username, username);
                                e.putString("password_" + username, password);
                                e.putBoolean("users_exist", true);

                                // Commit the new values with commit()
                                e.commit();
                                //Toast.makeText(this, "Successfully commit", Toast.LENGTH_LONG).show();

                                // Initialize  AsyncSignup() class with username and password
                                new AsyncSignup().execute(username, password);

                                // TODO: Add sha1 encryption to password on application and server-side.
                            } else {
                                // Password and username are the same
                                Toast.makeText(this, "Your password cannot be the same as your username", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            // Password do not match
                            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // Password too long
                        Toast.makeText(this, "Password must be less than 20 characters", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Password too short
                    Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                }
            } else {
                // No password supplied by user
                Toast.makeText(this, "Please enter a password", Toast.LENGTH_SHORT).show();
            }
        }
        else
        {
            // Check if password field is also blank
            if (passwordEntered)
            {
                // No username supplied by user
                Toast.makeText(this, "Please enter a username", Toast.LENGTH_SHORT).show();
            }
            else
            {
                // Both fields left blank by the user
                Toast.makeText(this, "Please enter a username and password", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onBackPressed()
    {
        // Exit application immediately
        moveTaskToBack(true);
    }


    private class AsyncSignup extends AsyncTask<String, String, String>
    {
        ProgressDialog pdLoading = new ProgressDialog(SignupActivity.this);
        HttpURLConnection conn;
        URL url = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // This method will be running on UI thread
            pdLoading.setMessage("\tCreating Account...");
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
                url = new URL("http://www.prawnskunk.com/pocketcleric/signup.inc.php");

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

                } else {

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

                /* Here launching another activity when login successful. If you persist login state
                use sharedPreferences of Android. and logout button to clear sharedPreferences.
                 */
                Toast.makeText(SignupActivity.this, "Account successfully created.", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(SignupActivity.this,LoginActivity.class);
                startActivity(intent);
                SignupActivity.this.finish();
            }
            else if (result.equalsIgnoreCase("exists")) {
                Toast.makeText(SignupActivity.this, "Username already exists.", Toast.LENGTH_LONG).show();
            }
            else if (result.equalsIgnoreCase("false")) {
                // If username and password does not match display a error message
                Toast.makeText(SignupActivity.this, "Failed to add new user.", Toast.LENGTH_LONG).show();
            }
            else if (result.equalsIgnoreCase("exception")) {
                Toast.makeText(SignupActivity.this, "An exception occurred.", Toast.LENGTH_LONG).show();
            }
            else if (result.equalsIgnoreCase("unsuccessful")) {
                Toast.makeText(SignupActivity.this, "Connection was unsuccessful.", Toast.LENGTH_LONG).show();
            }
        }
    }
}

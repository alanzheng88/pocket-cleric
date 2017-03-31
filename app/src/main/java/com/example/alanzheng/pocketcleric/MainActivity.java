package com.example.alanzheng.pocketcleric;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends Activity {

    // Declare instance variables
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String PREFS_TAG = "LoginData";
    private static final String DEFAULT = "NULL";

    public SharedPreferences sp;
    public boolean usersExist;
    public Intent i;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Clear shared login preferences
        // getSharedPreferences(PREFS_TAG, 0).edit().clear().commit();
        startActivity(new Intent(this, TrafficMonitorActivity.class));
    }

    // Triggers when SIGNUP Button clicked
    public void goToSignupActivity(View arg0) {
        // Create an intent for signup activity
        i = new Intent(this, SignupActivity.class);
        // Send user to next activity
        startActivity(i);
    }

    // Triggers when LOGIN Button clicked
    public void goToLoginActivity(View arg0) {
        // Create an intent for login activity
        i = new Intent(this, LoginActivity.class);
        // Send user to next activity
        startActivity(i);
    }

    // Not used currently (after shared preferences are implemented)
    public void goToNextActivity()
    {
        // Get shared preferences for reading
        sp = getSharedPreferences(PREFS_TAG, Context.MODE_PRIVATE);

        // Check if any accounts have been created (multi user signup per device)
        usersExist = sp.getBoolean("users_exist", false);

        // If shared preferences do not exist, send user to signup, else continue
        i = (usersExist) ? new Intent(this, LoginActivity.class) : new Intent(this, SignupActivity.class);

        // Send user to next activity
        startActivity(i);
    }
}



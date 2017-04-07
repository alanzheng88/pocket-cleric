package com.example.alanzheng.pocketcleric;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Debug;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    // Declare instance variables
    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Uncomment to clear shared login preferences
        //getSharedPreferences(PREFS_TAG, 0).edit().clear().commit();

        // Send user to next activity based on shared preferences
        goToNextActivityIfLoggedIn();

        // Uncomment to skip to tetherer monitor activity
        //startActivity(new Intent(this, TethererActivity.class));

        // Uncomment to skip to traffic monitor activity
        //startActivity(new Intent(this, TrafficMonitorActivity.class));
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        // Send user to next activity
        goToNextActivityIfLoggedIn();
    }

    // Triggers when SIGNUP Button clicked
    public void goToSignupActivity(View arg0) {
        // Create an intent for signup activity
        Intent i = new Intent(this, SignupActivity.class);
        // Send user to next activity
        startActivity(i);
    }

    // Triggers when LOGIN Button clicked
    public void goToLoginActivity(View arg0) {
        // Create an intent for login activity
        Intent i = new Intent(this, LoginActivity.class);
        // Send user to next activity
        startActivity(i);
    }

    // Triggers when GUEST Button clicked
    public void goToReceptorActivity(View view) {
        Intent i = new Intent(this, ReceptorActivity.class);
        startActivity(i);
    }

    // Not used currently (after shared preferences are implemented)
    public void goToNextActivityIfLoggedIn()
    {
        // Check if shared preferences exist
        Session session = new Session(getApplicationContext());
        if (session.isUserLoggedIn())
        {
            // If they do, next activity is the login activity
            Intent i = new Intent(this, TethererActivity.class);

            // Send user to next activity
            startActivity(i);
        }
    }
}



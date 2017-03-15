package com.example.alanzheng.pocketcleric;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends Activity {

    private static final String TAG = MainActivity.class.getSimpleName();


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    public void goToTethererActivity(View v) {
        Intent intent = new Intent(this, TethererActivity.class);
        startActivity(intent);
    }

}



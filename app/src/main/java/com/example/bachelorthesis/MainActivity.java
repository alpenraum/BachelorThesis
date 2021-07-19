package com.example.bachelorthesis;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends AppCompatActivity {
    private final static String INIT_LOC = "firstStart";

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //Set Toolbar as Appbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Lock orienttation based on device
        if(getResources().getBoolean(R.bool.portrait_only)){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT);
        }else{
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE);
        }

        //First launch?
        SharedPreferences sP =
                getSharedPreferences(getResources().getString(R.string.shared_pref_name),
                        Context.MODE_PRIVATE);

        if(sP.getBoolean(INIT_LOC,true)){
            Log.d("MainActivity","first time launch");

            loadData();

            SharedPreferences.Editor editor = sP.edit();
            editor.putBoolean(INIT_LOC,false);
            editor.apply();
        }
    }


    private void loadData(){

    }
}
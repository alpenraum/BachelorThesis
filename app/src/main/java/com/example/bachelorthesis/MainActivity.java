package com.example.bachelorthesis;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.material.bottomsheet.BottomSheetDialog;

public class MainActivity extends AppCompatActivity {
    private final static String INIT_LOC = "firstStart";

    private Fragment settingsFragment = null;

    private boolean settingsVisible = false;
    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //Set Toolbar as Appbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Lock orientation based on device
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
    private void showSettings(){
        View gray =  findViewById(R.id.fadeBackground);
        gray.setVisibility(View.VISIBLE);
        gray.animate().alpha(0.4f);

        settingsFragment= SettingsFragment.newInstance();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager
                .beginTransaction();

        fragmentTransaction.setCustomAnimations(R.anim.slide_in_right,R.anim.slide_out_right);
        fragmentTransaction.setReorderingAllowed(true).add(R.id.main_settings_fragment_container,
                settingsFragment).addToBackStack(null).commit();

        settingsVisible = true;
    }
    private void hideSettings(){

        getSupportFragmentManager().beginTransaction().remove(settingsFragment).commit();

        settingsFragment = null;

        View gray =  findViewById(R.id.fadeBackground);
        gray.animate().alpha(0.0f).withEndAction(() -> {gray.setVisibility(View.GONE);});

        settingsVisible = false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_settings:
                if(!settingsVisible) {
                    item.setIcon(R.drawable.settings_to_cross);
                    AnimatedVectorDrawable avd = (AnimatedVectorDrawable) item.getIcon();
                    avd.start();
                    showSettings();
                }else{
                    item.setIcon(R.drawable.cross_to_settings);
                    AnimatedVectorDrawable avd = (AnimatedVectorDrawable) item.getIcon();
                    avd.start();
                    hideSettings();
                }

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


}
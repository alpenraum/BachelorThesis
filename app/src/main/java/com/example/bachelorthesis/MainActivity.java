package com.example.bachelorthesis;

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

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.bachelorthesis.persistence.databases.AppDataBase;
import com.example.bachelorthesis.persistence.entities.Patient;
import com.example.bachelorthesis.utils.CSVParser;

import java.io.IOException;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {
    private final static String INIT_LOC = "firstStart";
    private final CompositeDisposable mDisposable = new CompositeDisposable();
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
        if (getResources().getBoolean(R.bool.portrait_only)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE);
        }



        //First launch?
        SharedPreferences sP =
                getSharedPreferences(getResources().getString(R.string.shared_pref_name),
                        Context.MODE_PRIVATE);

        if (sP.getBoolean(INIT_LOC, true)) {
            Log.d("MainActivity", "first time launch");

            loadData();
            SharedPreferences.Editor editor = sP.edit();
            editor.putBoolean(INIT_LOC, false);
            editor.apply();
        }

    }

    @Override
    protected void onStart() {
        super.onStart();

        Flowable<List<Patient>> patienFlowable =
                AppDataBase.getInstance(this).patientDAO().getAllPatients();

        mDisposable.add(patienFlowable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::updatePatientList, throwable -> Log.e("MainActivity", "Unable to" +
                                " get " +
                                "patients",
                        throwable))
        );
    }

    private void updatePatientList(List<Patient> patients){
        for (Patient p : patients
        ) {
            Log.d("PATIENT", p.name);
        }
    }


    @Override
    protected void onStop() {
        super.onStop();

        mDisposable.clear();
    }

    private void loadData() {


        //getAssets.open("filename");
        try {
            CSVParser.parsePatientFile(getAssets().open("08001362_Klaus_Penz_1962-06-12.csv"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        com.mmue21.hackysnake.utils.Concurrency.executeAsync(
                () -> {
                    for (int i = 0; i < 30; i++) {
                        AppDataBase.getInstance(this).patientDAO().insert(new Patient(8932L+i,
                                "test"+i));
                    }
                });

    }

    private void showSettings() {
        View gray = findViewById(R.id.fadeBackground);
        gray.setVisibility(View.VISIBLE);
        gray.animate().alpha(0.4f);

        settingsFragment = SettingsFragment.newInstance();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager
                .beginTransaction();

        fragmentTransaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right);
        fragmentTransaction.setReorderingAllowed(true).add(R.id.main_settings_fragment_container,
                settingsFragment).addToBackStack(null).commit();

        settingsVisible = true;
    }

    private void hideSettings() {

        getSupportFragmentManager().beginTransaction().remove(settingsFragment).commit();

        settingsFragment = null;

        View gray = findViewById(R.id.fadeBackground);
        gray.animate().alpha(0.0f).withEndAction(() -> {
            gray.setVisibility(View.GONE);
        });

        settingsVisible = false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_settings:
                if (!settingsVisible) {
                    item.setIcon(R.drawable.settings_to_cross);
                    AnimatedVectorDrawable avd = (AnimatedVectorDrawable) item.getIcon();
                    avd.start();
                    showSettings();
                } else {
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
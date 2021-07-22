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
import com.example.bachelorthesis.persistence.entities.PatientDataRecord;
import com.example.bachelorthesis.utils.CSVParser;
import com.example.bachelorthesis.utils.CSVPatientRecord;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

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

    private void updatePatientList(List<Patient> patients) {
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


        try {
            List<CSVPatientRecord> csvPatientRecords_Penz = CSVParser.parsePatientFile(
                    getAssets().open("08001362_Klaus_Penz_1962-06-12.csv"));

            List<CSVPatientRecord> csvPatientRecords_Doe = CSVParser.parsePatientFile(
                    getAssets().open("08001363_Jane_Doe_1972-06-12.csv"));

            //manually add the user data since it is not saved somewhere accessible;
            Patient patientPenz = new Patient("08001362", "Klaus Penz", new Date(1962, 6, 12));
            Patient patientDoe = new Patient("08001363", "Jane Doe", new Date(1972, 6, 12));
            AtomicLong idPenz = new AtomicLong();
            AtomicLong idDoe = new AtomicLong();
            com.mmue21.hackysnake.utils.Concurrency.executeAsync(
                    () -> {
                        idPenz.set(AppDataBase.getInstance(this).patientDAO().insert(patientPenz));
                        idDoe.set(AppDataBase.getInstance(this).patientDAO().insert(patientPenz));

                        AppDataBase.getInstance(this)
                                .patientDataDAO()
                                .insertPatientDataRecords(
                                        convertCSVPatientData(csvPatientRecords_Penz,
                                                idPenz.get()).toArray(
                                                new PatientDataRecord[csvPatientRecords_Penz.size()]));

                        AppDataBase.getInstance(this)
                                .patientDataDAO()
                                .insertPatientDataRecords(
                                        convertCSVPatientData(csvPatientRecords_Doe,
                                                idDoe.get()).toArray(
                                                new PatientDataRecord[csvPatientRecords_Doe.size()]));
                    });


        } catch (IOException e) {
            Log.e("CSV_PARSING", "Error while parsing csv file. " + e.getMessage());

            Snackbar.make(findViewById(R.id.main_activity_root), "Error while loading user data!",
                    Snackbar.LENGTH_SHORT).show();


        }


    }

    private List<PatientDataRecord> convertCSVPatientData(List<CSVPatientRecord> csvPatient,
                                                          long patientId) {
        ArrayList<PatientDataRecord> patientData = new ArrayList<>();

        for (CSVPatientRecord cP : csvPatient) {
            PatientDataRecord patientDataRecord = PatientDataRecord.generate(cP);
            patientDataRecord.patientId = patientId;
            patientData.add(patientDataRecord);
        }
        return patientData;
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
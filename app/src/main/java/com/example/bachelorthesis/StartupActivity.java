package com.example.bachelorthesis;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.example.bachelorthesis.persistence.databases.AppDataBase;
import com.example.bachelorthesis.persistence.entities.Patient;
import com.example.bachelorthesis.persistence.entities.PatientDataRecord;
import com.example.bachelorthesis.utils.CSVParser;
import com.example.bachelorthesis.utils.CSVPatientRecord;
import com.example.bachelorthesis.utils.Concurrency;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

public class StartupActivity extends AppCompatActivity {
    private final static String INIT_LOC = "firstStart";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //First launch?
        SharedPreferences sP =
                getSharedPreferences(getResources().getString(R.string.shared_pref_name),
                        Context.MODE_PRIVATE);

        if (!sP.getBoolean(INIT_LOC, true)) {
            Intent i = new Intent(this, MainActivity.class);
            startActivity(i);
        } else {
            SharedPreferences.Editor editor = sP.edit();
            editor.putBoolean(INIT_LOC, false);
            editor.apply();
        }
        setContentView(R.layout.activity_startup);
    }

    @Override
    protected void onResume() {
        super.onResume();


        Log.d("MainActivity", "first time launch");

        loadData();

        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);

    }

    private void loadData() {
        long time = System.currentTimeMillis();

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
            Future<?> f = Concurrency.executeAsync(
                    () -> {
                        idPenz.set(AppDataBase.getInstance(this).patientDAO().insert(patientPenz));
                        idDoe.set(AppDataBase.getInstance(this).patientDAO().insert(patientDoe));

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

            //Ugly Anti-Pattern. I do not want to over-engineer the data loading for a proof of concept
            while (!f.isDone()) {
                Log.d("Loading Data","Waiting on Thread Completion...");
            }

            Log.d("Loading Data", "Completion took "+(System.currentTimeMillis()-time)/1000.0f+" seconds.");


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
}
package com.example.bachelorthesis;

import android.annotation.SuppressLint;
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
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bachelorthesis.persistence.databases.AppDataBase;
import com.example.bachelorthesis.persistence.entities.Patient;
import com.example.bachelorthesis.PatientRecyclerView.PatientListAdapter;
import com.example.bachelorthesis.PatientRecyclerView.PatientVisualizationCallback;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity implements PatientVisualizationCallback {

    private final CompositeDisposable mDisposable = new CompositeDisposable();
    private Fragment settingsFragment = null;
    private boolean settingsVisible = false;

    private Fragment contentFragment =null;

    private PatientListAdapter patientListAdapter;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("MAIN_ACTIVITY","Main Activity launched");
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

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        patientListAdapter = new PatientListAdapter(new ArrayList<>(), this,layoutManager);

        RecyclerView recyclerView = findViewById(R.id.main_patient_recyclerview);
        recyclerView.setAdapter(patientListAdapter);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL));





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

        patientListAdapter.updateData(patienFlowable.blockingFirst());


    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void updatePatientList(List<Patient> patients) {
        patientListAdapter.updateData(patients);
        patientListAdapter.notifyDataSetChanged();
    }


    @Override
    protected void onStop() {
        super.onStop();

        mDisposable.clear();
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
        gray.animate().alpha(0.0f).withEndAction(() -> gray.setVisibility(View.GONE));

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
        if (item.getItemId() == R.id.action_settings) {
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
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void showPatientData(Patient patient) {

        if(contentFragment!=null) {
            if (contentFragment.isVisible()) {
                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager
                        .beginTransaction();

                fragmentTransaction.setReorderingAllowed(true).remove(contentFragment).commit();
                contentFragment = null;
            }
        }


        Log.d("Main_Activity","updating visualization");

        contentFragment = ContentFragment.newInstance(patient);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager
                .beginTransaction();

        fragmentTransaction.setReorderingAllowed(true).add(R.id.main_fragment_container,
                contentFragment).addToBackStack(null).commit();
    }
}
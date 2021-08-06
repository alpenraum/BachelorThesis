package com.example.bachelorthesis;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bachelorthesis.PatientRecyclerView.PatientListAdapter;
import com.example.bachelorthesis.PatientRecyclerView.PatientVisualizationCallback;
import com.example.bachelorthesis.exceptions.SearchInputException;
import com.example.bachelorthesis.persistence.databases.AppDataBase;
import com.example.bachelorthesis.persistence.entities.Patient;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity implements PatientVisualizationCallback {

    private final CompositeDisposable mDisposable = new CompositeDisposable();
    private Fragment settingsFragment = null;
    private boolean settingsVisible = false;

    private Fragment contentFragment = null;

    private PatientListAdapter patientListAdapter;

    private TextInputEditText searchBar;
    private TextInputLayout searchBarLayout;

    private List<Patient> patients;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("MAIN_ACTIVITY", "Main Activity launched");
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
        patientListAdapter = new PatientListAdapter(new ArrayList<>(), this, layoutManager);

        RecyclerView recyclerView = findViewById(R.id.main_patient_recyclerview);
        recyclerView.setAdapter(patientListAdapter);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(),
                DividerItemDecoration.VERTICAL));
        recyclerView.setMinimumWidth(225);


        searchBarLayout = findViewById(R.id.main_text_input_layout);
        searchBar = findViewById(R.id.search_edit_text);


        searchBarLayout.setEndIconOnClickListener(v -> {
            updatePatientList(patients);
            searchBar.setText("");
        });
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    if(!s.toString().isEmpty()) {
                        filterList(s);
                        searchBarLayout.setError(null);
                    }else{
                        updatePatientList(patients);
                    }

                } catch (SearchInputException e) {
                    if (SearchInputException.Type.NAME == (e.getType())) {
                        searchBarLayout.setError("Names do not contain numbers");
                    } else if (SearchInputException.Type.NUMBER == e.getType()) {
                        searchBarLayout.setError("Patient-nr. do not contain letters");
                    } else {
                        searchBarLayout.setError("Input only a name or number");
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        searchBar.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == KeyEvent.ACTION_DOWN) {

                    if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                        //ENTER
                        Log.d("MAIN","Close Keyboard through EditorActionListener");
                        closeKeyBoard();
                        return true;
                    }
                }

                return false;
            }
        });


    }

    private void closeKeyBoard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)
                    getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void filterList(CharSequence input) throws SearchInputException {
        String stringInput = input.toString();
        ArrayList<Patient> results = new ArrayList<>(patients);

        if (stringInput.matches("^[0-9]+$")) { //patient Number
            List<Patient> filteredResults =
                    results.stream().filter(patient ->
                            patient.patientNumber
                                    .toLowerCase(Locale.ROOT).contains(stringInput.toLowerCase(Locale.ROOT)))
                            .collect(Collectors.toList());

            updatePatientList(filteredResults);

        } else if (stringInput.matches("^[a-zA-Z\\-\\s]+$")) { //patient name

            List<Patient> filteredResults =
                    results.stream().filter(patient ->
                            patient.name
                                    .toLowerCase(Locale.ROOT).contains(stringInput.toLowerCase(Locale.ROOT)))
                            .collect(Collectors.toList());

            updatePatientList(filteredResults);
        } else {
            char[] chars = stringInput.toCharArray();
            int letters = 0;
            for (int i = 0; i < chars.length; i++) {
                if (letters >= chars.length / 2) { //more letters than digits -> name
                    throw new SearchInputException("No Valid input",
                            SearchInputException.Type.NAME);
                }
                if (String.valueOf(chars[i]).matches("^[a-zA-Z\\-\\s]+$")) {
                    letters++;
                }
            }

            throw new SearchInputException("No Valid input", SearchInputException.Type.NUMBER);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        Flowable<List<Patient>> patientFlowable =
                AppDataBase.getInstance(this).patientDAO().getAllPatients();

        mDisposable.add(patientFlowable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::updatePatientList, throwable -> Log.e("MainActivity", "Unable to" +
                                " get " +
                                "patients",
                        throwable))
        );


    }

    @Override
    protected void onResume() {
        super.onResume();


        Flowable<List<Patient>> patientFlowable =
                AppDataBase.getInstance(this).patientDAO().getAllPatients();
        patients = patientFlowable.blockingFirst();
        patientListAdapter.updateData(patients);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void updatePatientList(List<Patient> patients) {
        Log.d("Main Activity", "RECYLERVIEW UPDATED");
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

        if (contentFragment != null) {
            if (contentFragment.isVisible()) {
                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager
                        .beginTransaction();

                fragmentTransaction.setReorderingAllowed(true).remove(contentFragment).commit();
                contentFragment = null;
            }
        }


        Log.d("Main_Activity", "updating visualization");

        contentFragment = ContentFragment.newInstance(patient);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager
                .beginTransaction();

        fragmentTransaction.setReorderingAllowed(true).add(R.id.main_fragment_container,
                contentFragment).addToBackStack(null).commit();
    }
}
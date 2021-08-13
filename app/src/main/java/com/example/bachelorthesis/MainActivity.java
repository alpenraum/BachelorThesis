package com.example.bachelorthesis;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
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
import android.widget.Toast;

import androidx.annotation.Nullable;
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
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
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
    private String intentPatient = null;

    private Patient user;

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

            ExtendedFloatingActionButton addData = findViewById(R.id.add_data_fab);
            addData.setOnClickListener((view)-> showAddDataFragment());

        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE);


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
                        if (!s.toString().isEmpty()) {
                            filterList(s);
                            searchBarLayout.setError(null);
                        } else {
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
            searchBar.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == KeyEvent.ACTION_DOWN) {

                    if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                        //ENTER
                        Log.d("MAIN", "Close Keyboard through EditorActionListener");
                        closeKeyBoard();
                        return true;
                    }
                }

                return false;
            });


            //external intent receive
            // Get intent, action and MIME type
            Intent intent = getIntent();
            String action = intent.getAction();

            if (Intent.ACTION_VIEW.equals(action)) {

                Log.d("INTENT SCAN", intent.getDataString());

                //processScanResults(intent.getDataString()); // Handle text being sent
                intentPatient = intent.getDataString();

            }


            FloatingActionButton scanButton = findViewById(R.id.scanner_fab);
            scanButton.setOnClickListener(this::startScanner);

        }
        if(getResources().getBoolean(R.bool.portrait_only)) {
            Single<Patient> patient =
                    AppDataBase.getInstance(this).patientDAO().findPatientByPatientNumber(
                            "08001362");

            patient.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::handleUserFlowable, throwable -> {
                        Log.e("User Loading", "Error while loading the current user!");
                    });
        }
    }


    private void showAddDataFragment(){
        String[] measurements = new String[] {
                getString(R.string.m_bloodsugar),
                getString(R.string.m_bmi),
                getString(R.string.m_bodyweight),
                getString(R.string.bloodpressure),
                getString(R.string.m_hba1c),
                getString(R.string.m_triglyceride),
                getString(R.string.m_creatinin)
        };
        AddDataBottomSheet sheet = new AddDataBottomSheet(measurements, this.user.id);
        sheet.show(getSupportFragmentManager(),"addDataBottomSheet");


    }



    private void handleUserFlowable(Patient patient) {
        this.user = patient;
        showPatientData(patient);
    }

    public void startScanner(View v) {
        IntentIntegrator intentIntegrator = new IntentIntegrator(this);
        intentIntegrator.setPrompt("Scan a patient's code");
        intentIntegrator.setOrientationLocked(true);
        intentIntegrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        // if the intentResult is null then
        // toast a message as "cancelled"
        if (intentResult != null) {
            if (intentResult.getContents() == null) {
                Toast.makeText(getBaseContext(), "scan canceled!", Toast.LENGTH_SHORT).show();
            } else {
                if (BarcodeFormat.QR_CODE.name().equals(intentResult.getFormatName())
                        && intentResult.getContents().contains("patient://")) {


                    processScanResults(intentResult.getContents());
                } else {
                    Toast.makeText(getBaseContext(), "Invalid Code!", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void processScanResults(String result) {

        result = result.replace("patient://", "");
        try {
            //scroll to thing in recyclerview & highlight it & launch fragment (done via callback)
            int position = patientListAdapter.getPosition(result);
            if (position == -1) {
                throw new NoSuchElementException();
            }
            patientListAdapter.clickAndScrollToPosition(position);

            Toast.makeText(getBaseContext(), "Patient scanned succesfully!", Toast.LENGTH_SHORT).show();
        } catch (NoSuchElementException e) {
            Toast.makeText(getBaseContext(), "This QR-Code does not belong to any patient!",
                    Toast.LENGTH_SHORT).show();
        }

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
            for (char aChar : chars) {
                if (letters >= chars.length / 2) { //more letters than digits -> name
                    throw new SearchInputException("No Valid input",
                            SearchInputException.Type.NAME);
                }
                if (String.valueOf(aChar).matches("^[a-zA-Z\\-\\s]+$")) {
                    letters++;
                }
            }

            throw new SearchInputException("No Valid input", SearchInputException.Type.NUMBER);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!getResources().getBoolean(R.bool.portrait_only)) {
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

    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!getResources().getBoolean(R.bool.portrait_only)) {
            Flowable<List<Patient>> patientFlowable =
                    AppDataBase.getInstance(this).patientDAO().getAllPatients();
            patients = patientFlowable.blockingFirst();
            patientListAdapter.updateData(patients);

            if (intentPatient != null) {
                processScanResults(intentPatient);

            }
        }
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
        if (!getResources().getBoolean(R.bool.portrait_only)) {
            mDisposable.clear();
        }
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
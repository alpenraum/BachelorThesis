package com.example.bachelorthesis;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.bachelorthesis.persistence.databases.AppDataBase;
import com.example.bachelorthesis.persistence.entities.Patient;
import com.example.bachelorthesis.persistence.entities.PatientDataRecord;
import com.example.bachelorthesis.persistence.entities.relations.PatientWithData;
import com.example.bachelorthesis.utils.Concurrency;

import java.util.List;
import java.util.concurrent.Future;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ContentFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ContentFragment extends Fragment {

    private final OnDataLoadedCallback onDataLoadedCallback = this::updateDataset;
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PATIENT = "Patient";


    private Patient patient;
    private PatientWithData patientWithData;


    public ContentFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param patient The patient of which the data will be visualized.
     * @return A new instance of fragment ContentFragment.
     */
    public static ContentFragment newInstance(Patient patient) {
        ContentFragment fragment = new ContentFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_PATIENT, patient);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            patient = getArguments().getParcelable(ARG_PATIENT);
        }


    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_content, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();

        Future<?> f = Concurrency.executeAsync(() -> {
            PatientWithData data = AppDataBase.getInstance(getContext())
                    .patientDAO()
                    .findPatientWithData(patient.id);
            onDataLoadedCallback.onDataLoaded(data);
        });

        //Ugly Anti-Pattern. I do not want to over-engineer the data loading for a proof of concept
        while(!f.isDone()){

        }
        Log.d("Content-Fragment","PatientData Type at index 1: " + patientWithData.patientDataRecords.get(1).type);

        Log.d("Content-Fragment", "Working. Visualizing " + patient.name);
    }


    private void updateDataset(PatientWithData data) {
        this.patientWithData = data;
    }


    private interface OnDataLoadedCallback {
        void onDataLoaded(PatientWithData data);
    }
}
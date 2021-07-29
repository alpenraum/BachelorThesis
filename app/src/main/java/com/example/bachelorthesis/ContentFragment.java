package com.example.bachelorthesis;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.bachelorthesis.persistence.databases.AppDataBase;
import com.example.bachelorthesis.persistence.entities.Patient;
import com.example.bachelorthesis.persistence.entities.PatientDataRecord;
import com.example.bachelorthesis.persistence.entities.relations.PatientWithData;
import com.example.bachelorthesis.utils.Concurrency;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ContentFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
//TODO: FIND OUT HOW SCROLLVIEW IMPACTS THE USABILITY OF THE CHARTS
public class ContentFragment extends Fragment {

    private final OnDataLoadedCallback onDataLoadedCallback = this::updateDataset;
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PATIENT = "Patient";


    private Patient patient;
    private PatientWithData patientWithData;


    private LinearLayout chartLayout;

    private HashMap<String,View> chartViewMap = new HashMap<>();
    private HashMap<String,Chip> chipMap = new HashMap<>();


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


    private void generateChips(ChipGroup cG) {

        cG.addView(genChip(getResources().getString(R.string.treatment), cG));
        cG.addView(genChip(getResources().getString(R.string.risk), cG));
        cG.addView(genChip(getResources().getString(R.string.m_bloodsugar), cG));
        cG.addView(genChip(getResources().getString(R.string.m_bmi), cG));
        cG.addView(genChip(getResources().getString(R.string.m_bodyweight), cG));
        cG.addView(genChip(getResources().getString(R.string.bloodpressure), cG));
        cG.addView(genChip(getResources().getString(R.string.m_chol), cG));
        cG.addView(genChip(getResources().getString(R.string.m_creatinin), cG));
        cG.addView(genChip(getResources().getString(R.string.m_harn), cG));
        cG.addView(genChip(getResources().getString(R.string.m_hba1c), cG));
        cG.addView(genChip(getResources().getString(R.string.m_triglyceride), cG));

    }

    private Chip genChip(String name, ChipGroup cG) {
        Chip allChip = (Chip) getLayoutInflater().inflate(R.layout.chip_filter, cG, false);
        allChip.setText(name);

        //TODO: Set custom background color per chip

        allChip.setOnCheckedChangeListener((compoundButton, b) -> {
            Log.d("CONTENT_FRAGMENT", compoundButton.getText() + ": " + b);

            if (b) {
                showChart(String.valueOf(compoundButton.getText()));
            }else{
                hideChart(String.valueOf(compoundButton.getText()));
            }

        });
        chipMap.put(name,allChip);
        return allChip;
    }


    private List<Entry> getNumericData(String dataType) {
        List<Entry> result = new ArrayList<>();

        for (PatientDataRecord p : patientWithData.patientDataRecords
        ) {
            if (dataType.contains(p.type)) {
                result.add(new Entry(p.timeStamp.getTime(), Float.parseFloat(p.value1)));
            }
        }

        return result;
    }

    private float[] getMinMaxX(List<Entry> data) {
        float[] result = new float[2];

        data.sort((entry, t1) -> { //entry < t1 == -1 / entry = t1 == 0
            if(entry.getX()<t1.getX()){
                return -1;
            }else if(entry.getX() == t1.getX()){
                return 0;
            }
            return 1;
        });

        result[0] = data.get(0).getX();
        result[1] = data.get(data.size()-1).getX();

        return result;
    }

    private void hideChart(String dataName){
        if(chartViewMap.containsKey(dataName)){
            View chartView = chartViewMap.get(dataName);
            chartLayout.removeView(chartView);
            chartViewMap.remove(dataName);
        }
    }

    private void showChart(String dataName) {
        View chartLayoutView = getLayoutInflater().inflate(R.layout.linechart_layout, chartLayout,
                false);
        LineChart chart = chartLayoutView.findViewById(R.id.line_chart);
        ((TextView)chartLayoutView.findViewById(R.id.chart_title)).setText(dataName);
        chartLayoutView.findViewById(R.id.close_button).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(chipMap.containsKey(dataName)){
                            chipMap.get(dataName).setChecked(false);
                        }
                    }
                });
        chart.getDescription().setEnabled(false);

        // enable touch gestures
        chart.setTouchEnabled(true);

        chart.setDragDecelerationFrictionCoef(0.9f);

        // enable scaling and dragging
        chart.setDragYEnabled(true);
        chart.setScaleYEnabled(true);
        chart.setDragXEnabled(true);
        chart.setScaleXEnabled(true);
        chart.setDrawGridBackground(false);
        chart.setHighlightPerDragEnabled(true);

        List<Entry> data = getNumericData(dataName);
        float[] minMax = getMinMaxX(data);
        LineDataSet lineDataSet = new LineDataSet(data, dataName);
        lineDataSet.setColor(ColorTemplate.rgb("E69809"));

        chart.setData(new LineData(lineDataSet));
        chart.invalidate();

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.TOP_INSIDE);
        xAxis.setDrawAxisLine(false);
        xAxis.setDrawGridLines(true);
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            private final SimpleDateFormat mFormat = new SimpleDateFormat("dd.MM.yy",
                    Locale.ENGLISH);

            @Override
            public String getFormattedValue(float value, AxisBase axis) {


                return mFormat.format(new Date((long) value));
            }
        });

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART);

        leftAxis.setDrawGridLines(false);
        leftAxis.setGranularityEnabled(true);


        chart.getLegend().setEnabled(false);
        chart.setNoDataText("It is pretty empty here. \n Are you sure this patient is alive and in your supervision?");

        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setEnabled(false);


        chartViewMap.put(dataName,chartLayoutView);
        chartLayout.addView(chartLayoutView);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_content, container, false);

        chartLayout = view.findViewById(R.id.content_chart_layout);

        ChipGroup chipGroup = view.findViewById(R.id.content_chipgroup);
        generateChips(chipGroup);
        return view;
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
        while (!f.isDone()) {

        }
        Log.d("Content-Fragment",
                "PatientData Type at index 1: " + patientWithData.patientDataRecords.get(1).type);

        Log.d("Content-Fragment", "Working. Visualizing " + patient.name);
    }

    //interface for loading data with callback
    private void updateDataset(PatientWithData data) {
        this.patientWithData = data;
    }


    private interface OnDataLoadedCallback {
        void onDataLoaded(PatientWithData data);
    }
}
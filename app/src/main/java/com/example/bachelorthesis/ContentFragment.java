package com.example.bachelorthesis;

import android.annotation.SuppressLint;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.core.graphics.ColorUtils;
import androidx.fragment.app.Fragment;

import com.example.bachelorthesis.charts.LineMarkerView;
import com.example.bachelorthesis.charts.SyncChartsListener;
import com.example.bachelorthesis.persistence.Converters;
import com.example.bachelorthesis.persistence.databases.AppDataBase;
import com.example.bachelorthesis.persistence.entities.Patient;
import com.example.bachelorthesis.persistence.entities.PatientDataRecord;
import com.example.bachelorthesis.persistence.entities.relations.PatientWithData;
import com.example.bachelorthesis.utils.Concurrency;
import com.example.bachelorthesis.utils.CustomColorUtils;
import com.example.bachelorthesis.utils.DataType;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.ScatterChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.ScatterData;
import com.github.mikephil.charting.data.ScatterDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.interfaces.datasets.IScatterDataSet;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.jmedeisis.draglinearlayout.DragLinearLayout;
import com.leinardi.android.speeddial.SpeedDialView;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Future;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ContentFragment#newInstance} factory method to
 * create an instance of this fragment.
 */


public class ContentFragment extends Fragment {

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PATIENT = "Patient";

    //remember, it's a proof of concept.
    private final HashMap<String, Integer> scatterPlot_indexMap_Treatment = new HashMap<>();
    private final HashMap<String, Integer> scatterPlot_indexMap_Risk = new HashMap<>();


    private final HashMap<String, View> chartViewMap = new HashMap<>();
    private final HashMap<String, Chip> chipMap = new HashMap<>();
    private final List<Chart<?>> charts = new ArrayList<>();
    private Patient patient;
    private PatientWithData patientWithData;
    private final OnDataLoadedCallback onDataLoadedCallback = this::updateDataset;
    private DragLinearLayout chartLayout;
    private Long firstDataEntry = 0L;


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

        String[] treatments = new String[]{
                getString(R.string.t_acarb),
                getString(R.string.t_ace),
                getString(R.string.t_alt),
                getString(R.string.t_ass),
                getString(R.string.t_bbl),
                getString(R.string.t_dpp4),
                getString(R.string.t_glit),
                getString(R.string.t_met),
                getString(R.string.t_misch),
                getString(R.string.t_rr),
                getString(R.string.t_sh),
                getString(R.string.t_statin),
                getString(R.string.t_vzi),
        };

        for (int i = 0; i < treatments.length; i++) {
            scatterPlot_indexMap_Treatment.put(treatments[i], i);
        }

        String[] risks = new String[]{
                getString(R.string.m_harn),
                getString(R.string.r_insult),
                getString(R.string.r_khk),
                getString(R.string.r_nephro),
                getString(R.string.r_pavk),
                getString(R.string.r_retinopathie),
                getString(R.string.r_pnp)
        };
        for (int i = 0; i < risks.length; i++) {
            scatterPlot_indexMap_Risk.put(risks[i], i);
        }


    }


    private void generateChips(ChipGroup cG) {

        if (getResources().getBoolean(R.bool.portrait_only)) {
            cG.setSelectionRequired(true);
            cG.setSingleSelection(true);
        }
        cG.addView(genChip(getResources().getString(R.string.m_bloodsugar), cG));
        cG.addView(genChip(getResources().getString(R.string.m_bmi), cG));
        cG.addView(genChip(getResources().getString(R.string.m_bodyweight), cG));
        cG.addView(genChip(getResources().getString(R.string.bloodpressure), cG));
        cG.addView(genChip(getResources().getString(R.string.m_creatinin), cG));
        cG.addView(genChip(getResources().getString(R.string.m_hba1c), cG));
        cG.addView(genChip(getResources().getString(R.string.m_triglyceride), cG));

    }

    private Chip genChip(String name, ChipGroup cG) {
        Chip allChip = (Chip) getLayoutInflater().inflate(R.layout.chip_filter, cG, false);
        allChip.setText(name);


        int[][] states = new int[][]{
                new int[]{android.R.attr.state_checked},
                new int[]{-android.R.attr.state_checked}
        };
        int color = requireContext().getColor(DataType.valueOfName(name).getColor());
        int[] colors = new int[]{
                color,
                ColorUtils.setAlphaComponent(color, 255 / 3)
        };

        allChip.setChipBackgroundColor(new ColorStateList(states, colors));

        allChip.setOnCheckedChangeListener((compoundButton, b) -> {
            Log.d("CONTENT_FRAGMENT", compoundButton.getText() + ": " + b);

            if (b) {

                showChart(String.valueOf(compoundButton.getText()));
            } else {
                hideChart(String.valueOf(compoundButton.getText()));
            }

        });
        chipMap.put(name, allChip);
        return allChip;
    }


    private List<Entry> getNumericData(String dataType) {
        List<Entry> result = new ArrayList<>();

        for (PatientDataRecord p : patientWithData.patientDataRecords
        ) {
            if (dataType.contains(p.type)) {
                Entry e = new Entry(Converters.dateToTimestamp(p.timeStamp), Float.parseFloat(p.value1));
                e.setData(p);
                result.add(e);
            }
        }

        return result;
    }

    private List<Entry> getScatterChartData(String dataName, int index) {
        List<Entry> result = new ArrayList<>();

        for (PatientDataRecord p : patientWithData.patientDataRecords) {
            if (dataName.contains(p.type)) {
                Entry e = new Entry(Converters.dateToTimestamp(p.timeStamp), index);
                e.setData(p);
                result.add(e);
            }
        }
        return result;
    }

    private void sortData(List<Entry> data) {
        data.sort((entry, t1) -> { //entry < t1 == -1 / entry = t1 == 0
            if (entry.getX() < t1.getX()) {

                return -1;
            } else if (entry.getX() == t1.getX()) {
                return 0;
            }
            return 1;
        });
    }

    private void hideChart(String dataName) {
        if (chartViewMap.containsKey(dataName)) {
            View chartView = chartViewMap.get(dataName);
            //chartLayout.removeView(chartView);
            if (getResources().getBoolean(R.bool.portrait_only)) {
                chartLayout.removeView(chartView);
            } else {
                chartLayout.removeDragView(chartView);
            }
            chartViewMap.remove(dataName);
        }
    }

    /**
     * Factory method for creating LineCharts. It will not load any data and it will not add the
     * chart to the {@link ContentFragment#charts} List.
     * Both things must be done after calling this method.
     *
     * @param dataName the name of the Date which will be displayed by the chart
     * @return the configured Chart
     */
    private LineChart createLineChart(String dataName) {
        View chartLayoutView = getLayoutInflater().inflate(R.layout.linechart_layout, chartLayout,
                false);
        LineChart chart = chartLayoutView.findViewById(R.id.line_chart);
        ((TextView) chartLayoutView.findViewById(R.id.chart_title)).setText(dataName);

        if (getResources().getBoolean(R.bool.portrait_only)) {

            DataType d = DataType.valueOfName(dataName);
            ((CardView) chartLayoutView.findViewById(R.id.line_cardview))
                    .setCardBackgroundColor(requireContext().getColor(d.getColor()));
            int textColor = CustomColorUtils.isBrightColor(d.getColor()) ?
                    requireContext().getColor(R.color.black) :
                    requireContext().getColor(R.color.white);

            ((TextView) chartLayoutView.findViewById(R.id.chart_title)).setTextColor(textColor);
        } else {
            chartLayoutView.findViewById(R.id.close_button).setOnClickListener(
                    view -> {
                        if (chipMap.containsKey(dataName)) {
                            Objects.requireNonNull(chipMap.get(dataName)).setChecked(false);
                        }
                    });
        }
        chart.getDescription().setText(dataName);
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
        chart.setOnChartGestureListener(new SyncChartsListener(chart, this));

        //customize axes
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
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
        xAxis.setAxisMinimum(firstDataEntry);
        xAxis.setAxisMaximum(System.currentTimeMillis());

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        leftAxis.setDrawGridLines(false);
        leftAxis.setGranularityEnabled(true);

        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setEnabled(false);

        //disable legend
        chart.getLegend().setEnabled(true);
        chart.setNoDataText(
                "It is pretty empty here. \n Are you sure this patient is alive and in your supervision?");


        //text formatting size
        leftAxis.setTextSize(12.0f);
        xAxis.setTextSize(12.0f);

        //Set the add-data FAB
        if(!getResources().getBoolean(R.bool.portrait_only)) {
            FloatingActionButton fab = chartLayoutView.findViewById(R.id.linechart_add_data_fab);
            fab.setOnClickListener((view -> this.showAddDataDialog(dataName)));
        }


        chartViewMap.put(dataName, chartLayoutView);
        addViewToDragLinearLayout(chartLayoutView, chartLayout);

        return chart;
    }

    private ScatterChart createScatterChart(String dataName) {
        View chartLayoutView = getLayoutInflater().inflate(R.layout.scatterchart_layout,
                chartLayout,
                false);
        ScatterChart chart = chartLayoutView.findViewById(R.id.scatter_chart);
        ((TextView) chartLayoutView.findViewById(R.id.chart_title)).setText(dataName);
        chartLayoutView.findViewById(R.id.close_button).setOnClickListener(
                view -> {
                    if (chipMap.containsKey(dataName)) {
                        Objects.requireNonNull(chipMap.get(dataName)).setChecked(false);
                    }
                });
        chart.getDescription().setText(dataName);
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
        chart.setOnChartGestureListener(new SyncChartsListener(chart, this));

        //customize axes
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
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
        xAxis.setAxisMinimum(firstDataEntry);
        xAxis.setAxisMaximum(System.currentTimeMillis());

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        leftAxis.setDrawGridLines(true);
        leftAxis.setGranularityEnabled(true);

        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setEnabled(false);

        //disable legend
        chart.getLegend().setEnabled(true);
        chart.setNoDataText(
                "It is pretty empty here. \n Are you sure this patient is alive and in your supervision?");


        //text formatting size
        leftAxis.setTextSize(12.0f);
        xAxis.setTextSize(12.0f);

        chartViewMap.put(dataName, chartLayoutView);
        addViewToDragLinearLayout(chartLayoutView, chartLayout);
        return chart;
    }

    private void addViewToDragLinearLayout(View chartView, DragLinearLayout layout) {
        layout.addView(chartView);
        if (!getResources().getBoolean(R.bool.portrait_only)) {
            layout.setViewDraggable(chartView, chartView);
        }
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    private void genTreatmentScatterChart(String dataName) {
        ScatterChart chart = createScatterChart(dataName);

        //dataset
        List<IScatterDataSet> dataSets = new ArrayList<>();
        DataType d = DataType.valueOfName(dataName);
        for (Map.Entry<String, Integer> e : scatterPlot_indexMap_Treatment.entrySet()) {

            ScatterChart.ScatterShape scatterShape =
                    ScatterChart.ScatterShape
                            .values()[(e.getValue() % (ScatterChart.ScatterShape.values().length - 1))];

            List<Entry> data = getScatterChartData(e.getKey(), e.getValue());
            sortData(data);
            ScatterDataSet scatterDataSet = new ScatterDataSet(data, e.getKey());
            scatterDataSet.setScatterShape(scatterShape);
            scatterDataSet.setColor(requireContext().getColor(d.getColor()));
            scatterDataSet.setHighlightEnabled(true);
            scatterDataSet.setDrawValues(false);
            scatterDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);

            dataSets.add(scatterDataSet);

        }

        chart.getAxisLeft().setValueFormatter((value, axis) ->
                scatterPlot_indexMap_Treatment.entrySet().stream()
                        .filter(e -> e.getValue().equals((int) value))
                        .reduce((stringIntegerEntry, stringIntegerEntry2) -> stringIntegerEntry)
                        .get().getKey());

        chart.setData(new ScatterData(dataSets));
        chart.invalidate();
        chart.setMarker(new LineMarkerView(getContext(), R.layout.custom_marker,
                requireContext().getColor(d.getColor())));


        if (!charts.isEmpty()) {
            SyncChartsListener.syncCharts(charts.get(0), new Chart[]{chart});

        }

        charts.add(chart);
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    private void genRiskScatterChart(String dataName) {
        ScatterChart chart = createScatterChart(dataName);

        //dataset
        List<IScatterDataSet> dataSets = new ArrayList<>();
        DataType d = DataType.valueOfName(dataName);
        for (Map.Entry<String, Integer> e : scatterPlot_indexMap_Risk.entrySet()) {

            ScatterChart.ScatterShape scatterShape =
                    ScatterChart.ScatterShape
                            .values()[(e.getValue() % (ScatterChart.ScatterShape.values().length - 1))];

            List<Entry> data = getScatterChartData(e.getKey(), e.getValue());
            sortData(data);
            ScatterDataSet scatterDataSet = new ScatterDataSet(data, e.getKey());
            scatterDataSet.setScatterShape(scatterShape);
            scatterDataSet.setColor(requireContext().getColor(d.getColor()));
            scatterDataSet.setHighlightEnabled(true);
            scatterDataSet.setDrawValues(false);
            scatterDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);

            dataSets.add(scatterDataSet);

        }

        chart.getAxisLeft().setValueFormatter((value, axis) ->
                scatterPlot_indexMap_Risk.entrySet().stream()
                        .filter(e -> e.getValue().equals((int) value))
                        .reduce((stringIntegerEntry, stringIntegerEntry2) -> stringIntegerEntry)
                        .get().getKey());

        chart.setData(new ScatterData(dataSets));
        chart.invalidate();
        chart.setMarker(new LineMarkerView(getContext(), R.layout.custom_marker,
                requireContext().getColor(d.getColor())));


        if (!charts.isEmpty()) {
            SyncChartsListener.syncCharts(charts.get(0), new Chart[]{chart});

        }

        charts.add(chart);

    }

    private void genLineChart(String dataName) {
        LineChart chart = createLineChart(dataName);

        //dataset
        List<Entry> data = getNumericData(dataName);
        sortData(data);
        LineData lineData = new LineData();

        LineDataSet lineDataSet = new LineDataSet(data, dataName);
        DataType d = DataType.valueOfName(dataName);
        if (getResources().getBoolean(R.bool.portrait_only)) {
            lineDataSet.setColor(requireContext().getColor(R.color.primary));

            float avg = 0;
            for (Entry e: data
                 ) {
                avg+=e.getY();
            }
            avg= avg/data.size();
            float newestY = data.get(data.size()-1).getY();
            if(newestY>=avg*1.3f ||newestY<=avg*0.7f || newestY<d.getHealthyRange()[0] ||newestY>d.getHealthyRange()[1]){ //20% higher than average
                lineDataSet.setColor(requireContext().getColor(R.color.error_dark));
                Snackbar.make(requireContext(), requireView(),"Dangerous trend detected!",Snackbar.LENGTH_INDEFINITE).setAction(
                        "Acknowledge", view -> {}).setBackgroundTint(
                        requireContext().getColor(R.color.error_dark)).show();

            }









        } else {
            lineDataSet.setColor(requireContext().getColor(d.getColor()));
        }
        XAxis xAxis = chart.getXAxis();
        YAxis yAxis = chart.getAxisLeft();
        // draw limit lines behind data instead of on top
        yAxis.setDrawLimitLinesBehindData(true);
        xAxis.setDrawLimitLinesBehindData(true);


        LimitLine[] limitLines = getLimitLines(d,"lower Limit","upper Limit",requireContext().getColor(d.getColor()));
        yAxis.addLimitLine(limitLines[0]);
        yAxis.addLimitLine(limitLines[1]);


        lineDataSet.setHighlightEnabled(true);
        lineDataSet.setLineWidth(3.0f);
        lineDataSet.setDrawValues(false);
        lineDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        lineData.addDataSet(lineDataSet);
        chart.setData(lineData);
        chart.invalidate();
        chart.setMarker(new LineMarkerView(getContext(), R.layout.custom_marker,
                requireContext().getColor(d.getColor())));


        //text formatting
        lineDataSet.setValueTextSize(16.0f);


        if (!charts.isEmpty()) {
            SyncChartsListener.syncCharts(charts.get(0), new Chart[]{chart});
            chart.invalidate();
        }

        charts.add(chart);

    }

    private LimitLine[] getLimitLines(DataType type, String lowerLabel, String upperLabel, int color){

        LimitLine upperLimit= new LimitLine(type.getHealthyRange()[1], upperLabel);
        upperLimit.setLineWidth(1f);
        upperLimit.enableDashedLine(40f, 20f, 0f);
        upperLimit.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_TOP);
        upperLimit.setLineColor(color);


        LimitLine lowerLimit = new LimitLine(type.getHealthyRange()[0], lowerLabel);
        lowerLimit.setLineWidth(1f);
        lowerLimit.enableDashedLine(40f, 20f, 0f);
        lowerLimit.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_BOTTOM);
        lowerLimit.setLineColor(color);

        return new LimitLine[]{lowerLimit,upperLimit};
    }

    private void genBloodPressureChart(String dataName) {

        LineChart chart = createLineChart(dataName);

        List<Entry> dataDia = getNumericData(getString(R.string.m_bpdia));
        sortData(dataDia);
        LineDataSet lineDataSetDia = new LineDataSet(dataDia, getString(R.string.m_bpdia));
        DataType dDia = DataType.valueOfName(getString(R.string.m_bpdia));
        lineDataSetDia.setColor(requireContext().getColor(dDia.getColor()));
        lineDataSetDia.setHighlightEnabled(true);
        lineDataSetDia.setLineWidth(3.0f);
        lineDataSetDia.setDrawValues(false);
        lineDataSetDia.setAxisDependency(YAxis.AxisDependency.LEFT);

        List<Entry> dataSys = getNumericData(getString(R.string.m_bpsys));
        sortData(dataSys);
        LineDataSet lineDataSetSys = new LineDataSet(dataSys, getString(R.string.m_bpsys));
        DataType dSys = DataType.valueOfName(getString(R.string.m_bpsys));
        lineDataSetSys.setColor(requireContext().getColor(dSys.getColor()));
        lineDataSetSys.setHighlightEnabled(true);
        lineDataSetSys.setLineWidth(3.0f);
        lineDataSetSys.setDrawValues(false);
        lineDataSetSys.setAxisDependency(YAxis.AxisDependency.LEFT);


        if (getResources().getBoolean(R.bool.portrait_only)) {


            float avg = 0;
            for (Entry e : dataDia
            ) {
                avg += e.getY();
            }
            avg = avg / dataDia.size();
            float newestY = dataDia.get(dataDia.size() - 1).getY();
            if (newestY >= avg * 1.3f || newestY <= avg * 0.7f) { //20% higher than average
                Snackbar.make(requireContext(), requireView(), "Dangerous trend detected!",
                        Snackbar.LENGTH_INDEFINITE).setAction(
                        "Acknowledge", view -> {
                        }).setBackgroundTint(
                        requireContext().getColor(R.color.error_dark)).show();
            }




        }

        XAxis xAxis = chart.getXAxis();
        YAxis yAxis = chart.getAxisLeft();
        // draw limit lines behind data instead of on top
        yAxis.setDrawLimitLinesBehindData(true);
        xAxis.setDrawLimitLinesBehindData(true);


        LimitLine[] limitLinesDia = getLimitLines(dDia,"Diastolic lower Limit","Diastolic upper Limit",requireContext().getColor(dDia.getColor()));
        limitLinesDia[1].setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_BOTTOM);
        yAxis.addLimitLine(limitLinesDia[0]);
        yAxis.addLimitLine(limitLinesDia[1]);

        LimitLine[] limitLinesSys = getLimitLines(dSys,"Systolic lower Limit","Systolic upper Limit",requireContext().getColor(dSys.getColor()));
        limitLinesSys[0].setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_TOP);
        yAxis.addLimitLine(limitLinesSys[0]);
        yAxis.addLimitLine(limitLinesSys[1]);




        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(lineDataSetDia);
        dataSets.add(lineDataSetSys);

        chart.setData(new LineData(dataSets));
        chart.invalidate();
        chart.refreshDrawableState();

        chart.setMarker(new LineMarkerView(getContext(), R.layout.custom_marker,
                requireContext().getColor(dDia.getColor())));


        //text formatting size
        lineDataSetDia.setValueTextSize(16.0f);
        lineDataSetSys.setValueTextSize(16.0f);

        //sync the viewport to existing charts
        if (!charts.isEmpty()) {
            SyncChartsListener.syncCharts(charts.get(0), new Chart[]{chart});
        }

        charts.add(chart);

    }

    private void showChart(String dataName) {


        //Workaround. If this ever sees the light of a hospital many things need to be rewritten
        String[] linecharts = new String[]{
                "VS_bloodSugar", "DiabetesMeasureBMI", "VS_bodyWeight", "DiabetesMeasureCreatinin", "DiabetesMeasureHbA1c", "DiabetesMeasureTriglyceride", "ike_chol"
        };

        if (Arrays.asList(linecharts).contains(dataName)) {
            genLineChart(dataName);
        } else if ("Treatments".equals(dataName)) {
            genTreatmentScatterChart(dataName);
        } else if ("Risks".equals(dataName)) {
            genRiskScatterChart(dataName);
        } else if ("Blood Pressure".equals(dataName)) {
            genBloodPressureChart(dataName);
        }


    }


    public Chart<?>[] getOtherCharts(Chart<?> chart) {
        return charts.stream()
                .filter(chart1 -> !chart1.getDescription()
                        .getText()
                        .equals(chart.getDescription().getText()))
                .toArray(Chart[]::new);
    }


    @SuppressLint("NonConstantResourceId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_content, container, false);

        chartLayout = view.findViewById(R.id.content_chart_layout);

        ChipGroup chipGroup = view.findViewById(R.id.content_chipgroup);
        generateChips(chipGroup);


        chartLayout.setContainerScrollView(view.findViewById(R.id.content_chart_scrollview));


        SpeedDialView fab = view.findViewById(R.id.fab);
        fab.inflate(R.menu.granularity_fab_menu);
        fab.setOnActionSelectedListener(actionItem -> {

            switch (actionItem.getId()) {
                case R.id.last24h:
                    Log.d("FAB", "24H PRESSED");
                    showLast24H();
                    break;
                case R.id.last_week:
                    showLastWeek();
                    break;

                case R.id.last_month:
                    showLastMonth();
                    break;

                case R.id.allData:
                    showWholeData();
                    break;

                default:
                    return false;

            }

            fab.close();
            for (Chart<?> chart : charts) {
                chart.invalidate();
            }
            return true;
        });


        return view;
    }


    private void showLast24H() {
        LocalDate now = LocalDate.now();
        long yesterday = now.minusDays(1).atStartOfDay(ZoneId.systemDefault()).toEpochSecond() * 1000;

        if (!charts.isEmpty()) {
            showWholeData();
            LineChart chart = (LineChart) charts.get(0);
            float scaleFactor = (System.currentTimeMillis() - firstDataEntry) / (1000.0f * 60.0f * 60.0f * 24.0f);
            chart.zoom(scaleFactor, 1.0f, (System.currentTimeMillis() - yesterday) / 2.0f, 0.0f);
            SyncChartsListener.syncCharts(chart, getOtherCharts(chart));
        }
    }

    private void showLastWeek() {
        LocalDate now = LocalDate.now();
        long lastWeek = now.minusWeeks(1).atStartOfDay(ZoneId.systemDefault()).toEpochSecond() * 1000;

        if (!charts.isEmpty()) {
            showWholeData();
            LineChart chart = (LineChart) charts.get(0);
            float scaleFactor = (System.currentTimeMillis() - firstDataEntry) / (1000.0f * 60.0f * 60.0f * 24.0f);
            chart.zoom(scaleFactor / 7.0f, 1.0f, (System.currentTimeMillis() - lastWeek) / 2.0f, 0.0f);
            SyncChartsListener.syncCharts(chart, getOtherCharts(chart));
        }
    }

    private void showLastMonth() {
        LocalDate now = LocalDate.now();
        long lastMonth = now.minusMonths(1).atStartOfDay(ZoneId.systemDefault()).toEpochSecond() * 1000;
        if (!charts.isEmpty()) {

            showWholeData();
            LineChart chart = (LineChart) charts.get(0);
            float scaleFactor = (System.currentTimeMillis() - firstDataEntry) / (1000.0f * 60.0f * 60.0f * 24.0f);
            chart.zoom(scaleFactor / 30.0f, 1.0f, (System.currentTimeMillis() - lastMonth) / 2.0f, 0.0f);
            SyncChartsListener.syncCharts(chart, getOtherCharts(chart));
        }
    }

    private void showWholeData() {
        if (!charts.isEmpty()) {
            LineChart chart = (LineChart) charts.get(0);
            chart.zoom(0.00000000001f, 1.0f, 0.0f, 0.0f);
            SyncChartsListener.syncCharts(chart, getOtherCharts(chart));
        }
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

        firstDataEntry = Converters.dateToTimestamp(
                Collections.min(patientWithData.patientDataRecords,
                (entry, t1) -> { //entry < t1 == -1 / entry = t1 == 0
                    if (entry.timeStamp.isBefore(t1.timeStamp)) {

                        return -1;
                    } else if (entry.timeStamp.isAfter(t1.timeStamp)) {
                        return 1;
                    }
                    return 0;
                }).timeStamp);

        if(getResources().getBoolean(R.bool.portrait_only)) {
            Objects.requireNonNull(chipMap.get(getString(R.string.m_bloodsugar))).setChecked(true);
        }

        for (Map.Entry<String, Chip> stringChipEntry : chipMap.entrySet()) {
           String dataName;
            if(stringChipEntry.getKey().equals(getString(R.string.bloodpressure))){
                dataName = getString(R.string.m_bpdia);
            }else {
               dataName = stringChipEntry.getKey();
            }
            List<Entry> data = getNumericData(dataName);
            float avg = 0;
            Entry result = data.get(0);

            for (int i=0;i<data.size();i++){
                avg+=data.get(i).getY();
                if(result.getX()<data.get(i).getX()){
                    result = data.get(i);
                }
            }
            avg = avg/data.size();
            float[] healthyRange = DataType.valueOfName(dataName).getHealthyRange();
            if(result.getY()<avg*0.7f || result.getY()>avg*1.3f || result.getY()<healthyRange[0] ||result.getY()>healthyRange[1]){
                stringChipEntry.getValue().setChipIconVisible(true);
            }
        }


    }


    //interface for loading data with callback
    private void updateDataset(PatientWithData data) {
        this.patientWithData = data;
    }


    private interface OnDataLoadedCallback {
        void onDataLoaded(PatientWithData data);
    }



    private void showAddDataDialog(String dataName){
        AddDataBottomSheet sheet = new AddDataBottomSheet(dataName, patient.id,
               ()->{});
        sheet.show(getParentFragmentManager(), "addDataBottomSheet");
        Log.d("FRAGMENT CONTENT","SHOW ADD DATA DIALOG");
    }


}
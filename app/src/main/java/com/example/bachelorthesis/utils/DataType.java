package com.example.bachelorthesis.utils;

import com.example.bachelorthesis.R;

import java.util.HashMap;
import java.util.Map;

public enum DataType {

    TREATMENT("Treatments", R.color.data_treatment, new float[]{0,0}),
    BLOODPRESSURE("Blood Pressure", R.color.data_bloodpressure, new float[]{0,0}),
    BLOODSUGAR("VS_bloodSugar", R.color.data_bloodsugar, new float[]{80,150}),
    BMI("DiabetesMeasureBMI", R.color.data_bmi, new float[]{18,26}),
    BODYWEIGHT("VS_bodyWeight", R.color.data_weight, new float[]{0,200}),
    BPDIA("VS_bloodPressure_dia", R.color.data_dia, new float[]{60,80}),
    BPSYS("VS_bloodPressure_sys", R.color.data_sys, new float[]{90,120}),
    CHOL("ike_Chol", R.color.data_chol, new float[]{0,0}),
    CREATININ("DiabetesMeasureCreatinin", R.color.data_creatinin, new float[]{0.5f,1.1f}),
    HARN("ike_Harn", R.color.data_harn, new float[]{0,0}),
    HBA1C("DiabetesMeasureHbA1c", R.color.data_hba1c, new float[]{5,8.5f}),
    TRIGLYCERIDE("DiabetesMeasureTriglyceride", R.color.data_triglyceride, new float[]{40,140}),
    RISK("Risks", R.color.data_risk, new float[]{0,0});


    private static final Map<String, DataType> BY_NAME = new HashMap<>();

    static {
        for (DataType d : values()) {
            BY_NAME.put(d.name, d);
        }
    }

    private final String name;
    private final int color;
    private final float[] healthyRange;

    DataType(String name, int color, float[] healthyRange) {
        this.name = name;
        this.color = color;
        this.healthyRange = healthyRange;

    }

    public static DataType valueOfName(String name) {
        return BY_NAME.get(name);
    }

    public int getColor() {
        return color;
    }

    public String getName() {
        return name;
    }

    public float[] getHealthyRange() {
        return healthyRange;
    }
}

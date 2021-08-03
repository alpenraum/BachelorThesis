package com.example.bachelorthesis.utils;

import com.example.bachelorthesis.R;

import java.util.HashMap;
import java.util.Map;

public enum DataType {

    TREATMENT("Treatments",R.color.data_treatment),
    BLOODPRESSURE("Blood Pressure",R.color.data_bloodpressure),
    BLOODSUGAR("VS_bloodSugar",R.color.data_bloodsugar),
    BMI("DiabetesMeasureBMI",R.color.data_bmi),
    BODYWEIGHT("VS_bodyWeight",R.color.data_weight),
    BPDIA("VS_bloodPressure_dia",R.color.data_dia),
    BPSYS("VS_bloodPressure_sys",R.color.data_sys),
    CHOL("ike_Chol",R.color.data_chol),
    CREATININ("DiabetesMeasureCreatinin",R.color.data_creatinin),
    HARN("ike_Harn",R.color.data_harn),
    HBA1C("DiabetesMeasureHbA1c",R.color.data_hba1c),
    TRIGLYCERIDE("DiabetesMeasureTriglyceride",R.color.data_triglyceride),
    RISK("Risks",R.color.data_risk);


    private String name;
    private int color;

    private DataType(String name, int color){
        this.name = name;
        this.color = color;
    }

    private static final Map<String, DataType> BY_NAME = new HashMap<>();

    static {
        for (DataType d: values()) {
            BY_NAME.put(d.name, d);
        }
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

}

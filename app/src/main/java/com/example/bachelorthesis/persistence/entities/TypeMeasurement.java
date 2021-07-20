package com.example.bachelorthesis.persistence.entities;

import java.util.HashMap;
import java.util.Map;

public enum TypeMeasurement {
    HbA1c("DiabetesMeasureHbA1c"),
    BMI("DiabetesMeasureBMI"),
    bodyWeight("VS_bodyWeight"),
    bloodSugar("VS_bloodSugar"),
    Triglyceride("DiabetesMeasureTriglyceride"),
    Creatinin("DiabetesMeasureCreatinin"),
    bloodPressure_sys("VS_bloodPressure_sys"),
    bloodPressure_dia("VS_bloodPressure_dia");

    private String dataName;

    TypeMeasurement(String dataName) {
        this.dataName = dataName;
    }
    public String getDataName() {
        return this.dataName;
    }
    
    public static TypeMeasurement getEnumByDataName(String dataName){
        for (TypeMeasurement t: values()
             ) {
            if(t.getDataName().equals(dataName)){
                return t;
            }
        }
        return null;
    }
}

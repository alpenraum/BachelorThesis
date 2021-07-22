package com.example.bachelorthesis.utils;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvDate;

import java.util.Date;

/**
 * @author Finn Zimmer
 */
public class CSVPatientRecord {

    @CsvBindByName(column = "IDRELATION1")
    private long patientNumber;

    @CsvBindByName(column = "CRETIME")
    @CsvDate("yyyy-mm-dd")
    private Date createdAt;

    @CsvBindByName(column = "MCCOSITEM")
    private String type;

    @CsvBindByName(column = "VALUE1")
    private String value1;

    @CsvBindByName(column = "VALUE2")
    private String value2;

    public long getPatientNumber() {
        return patientNumber;
    }

    public void setPatientNumber(long patientNumber) {
        this.patientNumber = patientNumber;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getValue1() {
        return value1;
    }

    public void setValue1(String value1) {
        this.value1 = value1;
    }

    public String getValue2() {
        return value2;
    }

    public void setValue2(String value2) {
        this.value2 = value2;
    }
}

package com.example.bachelorthesis.persistence.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

/**
 * @author Finn Zimmer
 */
@Entity(tableName = "patient")
public class Patient {
    @PrimaryKey(autoGenerate = true)
    public long id;

    /*I do not like this but inserting the specified id into the
    / auto-increment field screams problems*/
    @ColumnInfo(name = "patient_number")
    public String patientNumber;

    public String name;

    public Date birthdate;

    public Patient(String patientNumber, String name, Date birthdate) {
        this.patientNumber = patientNumber;
        this.name = name;
        this.birthdate = birthdate;
    }
}

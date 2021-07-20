package com.example.bachelorthesis.persistence.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

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
    public long patientNumber;

    public String name;


}

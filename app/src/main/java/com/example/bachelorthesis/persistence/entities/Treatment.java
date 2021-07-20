package com.example.bachelorthesis.persistence.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * @author Finn Zimmer
 */
@Entity
public class Treatment {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "patient_id")
    public long patientId;

    public String type;

    public String value1;
    public String value2;


}

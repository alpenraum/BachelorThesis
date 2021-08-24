package com.example.bachelorthesis.persistence.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.example.bachelorthesis.utils.CSVPatientRecord;

import java.time.LocalDate;
import java.util.Date;
import java.util.Locale;

/**
 * @author Finn Zimmer
 */
@Entity(tableName = "patient_data_record")
public class PatientDataRecord {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "patient_id")
    public long patientId;


    public String type;

    @ColumnInfo(name = "time_stamp")
    public LocalDate timeStamp;

    public String value1;

    public String value2;


    public PatientDataRecord(long patientId, String type, LocalDate timeStamp,
                             String value1, String value2) {
        this.patientId = patientId;
        this.type = type;
        this.timeStamp = timeStamp;
        this.value1 = value1.replaceAll(",", ".");
        this.value2 = value2.replaceAll(",", ".");
    }

    public static PatientDataRecord generate(CSVPatientRecord p) {
        return new PatientDataRecord(-1L, p.getType(), p.getCreatedAt(),
                p.getValue1(), p.getValue2());
    }


}


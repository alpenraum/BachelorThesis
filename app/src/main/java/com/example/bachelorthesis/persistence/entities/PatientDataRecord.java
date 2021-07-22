package com.example.bachelorthesis.persistence.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.example.bachelorthesis.utils.CSVPatientRecord;

import java.util.Date;

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

    @ColumnInfo(name ="time_stamp")
    public Date timeStamp;

    public String value1;

    public String value2;



    public PatientDataRecord(long patientId,String type, Date timeStamp,
                             String value1, String value2) {
        this.patientId = patientId;
        this.type = type;
        this.timeStamp = timeStamp;
        this.value1 = value1;
        this.value2 = value2;
    }

    public static PatientDataRecord generate(CSVPatientRecord p){
        return new PatientDataRecord(-1L,p.getType(),p.getCreatedAt(),
                p.getValue1(),p.getValue2());
    }


}


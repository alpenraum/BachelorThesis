package com.example.bachelorthesis.persistence.entities.relations;

import androidx.room.Embedded;
import androidx.room.Relation;

import com.example.bachelorthesis.persistence.entities.Patient;
import com.example.bachelorthesis.persistence.entities.PatientDataRecord;

import java.util.List;

/**
 * @author Finn Zimmer
 */
public class PatientWithData {
    @Embedded
    public Patient patient;

    @Relation(parentColumn = "id", entityColumn = "patient_id", entity = PatientDataRecord.class)
    public List<PatientDataRecord> patientDataRecords;


}

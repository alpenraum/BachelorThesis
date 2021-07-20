package com.example.bachelorthesis.persistence.entities.relations;

import androidx.room.Embedded;
import androidx.room.Relation;

import com.example.bachelorthesis.persistence.entities.Measurement;
import com.example.bachelorthesis.persistence.entities.Patient;
import com.example.bachelorthesis.persistence.entities.Risk;
import com.example.bachelorthesis.persistence.entities.Treatment;

import java.util.List;

/**
 * @author Finn Zimmer
 */
public class PatientWithData {
    @Embedded
    public Patient patient;

    @Relation(parentColumn = "id",entityColumn = "patient_id", entity = Measurement.class)
    public List<Measurement> measurements;

    @Relation(parentColumn = "id",entityColumn = "patient_id", entity = Treatment.class)
    public List<Treatment> treatments;

    @Relation(parentColumn = "id",entityColumn = "patient_id", entity = Risk.class)
    public List<Risk> risks;

}

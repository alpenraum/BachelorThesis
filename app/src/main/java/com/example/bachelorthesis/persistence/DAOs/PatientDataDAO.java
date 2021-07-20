package com.example.bachelorthesis.persistence.DAOs;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.bachelorthesis.persistence.entities.Measurement;
import com.example.bachelorthesis.persistence.entities.Risk;
import com.example.bachelorthesis.persistence.entities.Treatment;

/**
 * @author Finn Zimmer
 */
@Dao
public interface PatientDataDAO {

    @Insert
    void insertMeasurement(Measurement measurement);

    @Insert
    void insertTreatment(Treatment treatment);

    @Insert
    void insertRisk(Risk risk);

    @Query("DELETE FROM Measurement")
    void nukeMeasurements();

    @Query("DELETE FROM Treatment")
    void nukeTreatments();

    @Query("DELETE FROM Risk")
    void nukeRisks();

    default void nuke(){
        nukeMeasurements();
        nukeRisks();
        nukeTreatments();
    }
}

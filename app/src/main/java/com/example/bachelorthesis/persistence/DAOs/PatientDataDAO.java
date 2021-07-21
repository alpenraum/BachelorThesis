package com.example.bachelorthesis.persistence.DAOs;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.bachelorthesis.persistence.entities.Measurement;
import com.example.bachelorthesis.persistence.entities.Risk;
import com.example.bachelorthesis.persistence.entities.Treatment;

import java.util.concurrent.CompletableFuture;

import io.reactivex.rxjava3.core.Completable;

/**
 * @author Finn Zimmer
 */
@Dao
public interface PatientDataDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertMeasurement(Measurement measurement);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertTreatment(Treatment treatment);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
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

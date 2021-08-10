package com.example.bachelorthesis.persistence.DAOs;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import com.example.bachelorthesis.persistence.entities.Patient;
import com.example.bachelorthesis.persistence.entities.relations.PatientWithData;

import java.util.List;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

/**
 * @author Finn Zimmer
 */
@Dao
public interface PatientDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Patient patient);

    @Query("SELECT * from patient ORDER BY name ASC")
    Flowable<List<Patient>> getAllPatients();

    @Transaction
    @Query("Select * from patient where id = :id")
    PatientWithData findPatientWithData(long id);

    @Query("Select * from patient where patient_number =:number")
    Single<Patient> findPatientByPatientNumber(String number);

    @Query("Delete from patient")
    void nukePatients();
}

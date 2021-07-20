package com.example.bachelorthesis.persistence.DAOs;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;

import com.example.bachelorthesis.persistence.entities.Patient;
import com.example.bachelorthesis.persistence.entities.relations.PatientWithData;

import java.util.List;

/**
 * @author Finn Zimmer
 */
@Dao
public interface PatientDAO {

    @Insert
    long insert(Patient patient);

    @Query("SELECT * from patient ORDER BY name ASC")
    List<Patient> selectAllPatients();

    @Transaction
    @Query("Select * from patient where id = :id")
    PatientWithData findPatientWithData(int id);

    @Query("Delete from patient")
    void nukePatients();
}

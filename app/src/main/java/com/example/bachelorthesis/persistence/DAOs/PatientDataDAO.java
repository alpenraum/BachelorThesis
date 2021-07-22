package com.example.bachelorthesis.persistence.DAOs;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.bachelorthesis.persistence.entities.PatientDataRecord;

/**
 * @author Finn Zimmer
 */
@Dao
public interface PatientDataDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertPatientDataRecord(PatientDataRecord patientDataRecord);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertPatientDataRecords(PatientDataRecord... patientDataRecords);

    @Query("DELETE FROM patient_data_record")
    public void nukeTable();

}

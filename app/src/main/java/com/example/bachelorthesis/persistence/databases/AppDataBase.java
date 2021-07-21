package com.example.bachelorthesis.persistence.databases;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.bachelorthesis.persistence.Converters;
import com.example.bachelorthesis.persistence.DAOs.PatientDAO;
import com.example.bachelorthesis.persistence.DAOs.PatientDataDAO;
import com.example.bachelorthesis.persistence.DAOs.TemplateDAO;
import com.example.bachelorthesis.persistence.entities.Measurement;
import com.example.bachelorthesis.persistence.entities.Patient;
import com.example.bachelorthesis.persistence.entities.Risk;
import com.example.bachelorthesis.persistence.entities.Treatment;
import com.example.bachelorthesis.persistence.entities.template;

import java.util.HashMap;

/**
 * @author Finn Zimmer
 */
@Database(entities = {Patient.class, Measurement.class, Risk.class, Treatment.class}, version=1,
        exportSchema = false)
@TypeConverters({Converters.class})
public abstract class AppDataBase extends RoomDatabase {

    public abstract PatientDAO patientDAO();
    public abstract PatientDataDAO patientDataDAO();

    private static HashMap<Context, AppDataBase> INSTANCES = new HashMap<>();

    public static AppDataBase getInstance(Context context){
        AppDataBase db = INSTANCES.get(context);
        if(db==null){
            db = Room.databaseBuilder(context, AppDataBase.class, "appData").build();
            INSTANCES.put(context,db);
        }
        return db;
    }
}

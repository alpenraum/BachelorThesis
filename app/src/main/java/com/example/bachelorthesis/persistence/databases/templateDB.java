package com.example.bachelorthesis.persistence.databases;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.bachelorthesis.persistence.DAOs.TemplateDAO;
import com.example.bachelorthesis.persistence.entities.template;

import java.util.HashMap;

/**
 * @author Finn Zimmer
 */
@Database(entities = {template.class}, version=1, exportSchema = false)
public abstract class templateDB extends RoomDatabase {

    public abstract TemplateDAO templateDAO();

    private static HashMap<Context, templateDB> INSTANCES = new HashMap<>();

    public static templateDB getInstance(Context context){
        templateDB db = INSTANCES.get(context);
        if(db==null){
            db = Room.databaseBuilder(context, templateDB.class, "template_db").build();
            INSTANCES.put(context,db);
        }
        return db;
    }
}

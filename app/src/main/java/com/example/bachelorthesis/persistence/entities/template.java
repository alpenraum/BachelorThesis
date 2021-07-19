package com.example.bachelorthesis.persistence.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * @author Finn Zimmer (11779643)
 */
@Entity(tableName = "template")
public class template {

        @PrimaryKey(autoGenerate = true)
        public int id;

        public String name;
        @NonNull
        public int difficulty;
        @NonNull
        public long score;

        public template(String name, int difficulty, long score){
            this.name = name;
            this.score = score;
            this.difficulty = difficulty;
        }

}

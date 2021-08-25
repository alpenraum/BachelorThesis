package com.example.bachelorthesis.persistence.entities;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * @author Finn Zimmer
 */
@Entity(tableName = "patient")
public class Patient implements Parcelable {
    public static final Creator<Patient> CREATOR = new Creator<Patient>() {
        @Override
        public Patient createFromParcel(Parcel in) {
            return new Patient(in);
        }

        @Override
        public Patient[] newArray(int size) {
            return new Patient[size];
        }
    };
    @PrimaryKey(autoGenerate = true)
    public long id;

    /*I do not like this but inserting the specified id into the
    / auto-increment field screams problems*/
    @ColumnInfo(name = "patient_number")
    public String patientNumber;

    public String name;

    public LocalDate birthdate;

    public Patient(String patientNumber, String name, LocalDate birthdate) {
        this.patientNumber = patientNumber;
        this.name = name;
        this.birthdate = birthdate;
    }

    protected Patient(Parcel in) {
        id = in.readLong();
        patientNumber = in.readString();
        name = in.readString();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                .withLocale(Locale.ENGLISH);
        birthdate = LocalDate.parse(in.readString(), formatter);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(id);
        parcel.writeString(patientNumber);
        parcel.writeString(name);
        parcel.writeString(birthdate.toString());
    }
}

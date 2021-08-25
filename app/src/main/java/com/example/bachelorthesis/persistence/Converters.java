package com.example.bachelorthesis.persistence;

import androidx.room.TypeConverter;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

/**
 * @author Finn Zimmer
 */
public class Converters {

    @TypeConverter
    public static LocalDate fromTimestamp(Long value) {
        if (value != null) {
            return Instant.ofEpochMilli(value).atZone(ZoneId.systemDefault()).toLocalDate();
        }
        return null;
    }

    @TypeConverter
    public static Long dateToTimestamp(LocalDate date) {

        if (date != null) {
            return date.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        }
        return null;
    }


}

package com.example.bachelorthesis.persistence;
import androidx.room.TypeConverter;
import com.example.bachelorthesis.persistence.entities.TypeMeasurement;

/**
 * @author Finn Zimmer
 */
public class Converters {

    @TypeConverter
    public static TypeMeasurement measurement_stringToType(String type) {

        if (type.isEmpty()) {
            return null;
        }

        return TypeMeasurement.valueOf(type);
    }

    @TypeConverter
    public static String measurement_typeToString(TypeMeasurement t) {
        if (t == null) {
            return null;
        }

        return t.name();
    }
}

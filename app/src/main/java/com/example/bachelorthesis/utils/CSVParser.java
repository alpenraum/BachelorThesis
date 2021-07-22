package com.example.bachelorthesis.utils;

import com.opencsv.bean.CsvToBeanBuilder;

import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

/**
 * @author Finn Zimmer
 */
public class CSVParser {

    public static List<CSVPatientRecord> parsePatientFile(InputStream in){

        return new CsvToBeanBuilder<CSVPatientRecord>(new InputStreamReader(in))
                .withType(CSVPatientRecord.class).build().parse();
    }
}

package com.telecom.inventory.util;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;
import lombok.extern.slf4j.Slf4j;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class CsvUtils {

    private CsvUtils() {
        // Private constructor to prevent instantiation
    }

    public static List<String[]> readCsvFile(String filePath) throws IOException, CsvValidationException {
        List<String[]> records = new ArrayList<>();
        
        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            String[] line;
            while ((line = reader.readNext()) != null) {
                records.add(line);
            }
        }
        
        return records;
    }

    public static void writeCsvFile(String filePath, List<String[]> records) throws IOException {
        try (CSVWriter writer = new CSVWriter(new FileWriter(filePath))) {
            writer.writeAll(records);
        }
    }

    public static String[] getHeader(String filePath) throws IOException, CsvValidationException {
        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            return reader.readNext();
        }
    }

    public static int getColumnIndex(String[] header, String columnName) {
        for (int i = 0; i < header.length; i++) {
            if (header[i].equalsIgnoreCase(columnName)) {
                return i;
            }
        }
        return -1;
    }
}

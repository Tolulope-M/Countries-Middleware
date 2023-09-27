package com.klasha.utils;

import com.klasha.dto.request.ExchangeRate;
import com.klasha.exception.ServerSideException;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.List;

public class CsvReader {
    public static List<ExchangeRate> readCSV(String filePath ) {
        ClassPathResource resource = new ClassPathResource(filePath);
        try (CSVReader reader = new CSVReader(new InputStreamReader(resource.getInputStream()))) {
            reader.skip(1);
            return reader.readAll().stream()
                    .map(CsvReader::mapToExchangeRate)
                    .toList();
        }catch (Exception e){
            throw new ServerSideException(e.getMessage());
        }
    }

    private static ExchangeRate mapToExchangeRate(String[] row) {
        ExchangeRate exchangeRate = new ExchangeRate();
        exchangeRate.setSourceCurrency(row[0]);
        exchangeRate.setTargetCurrency(row[1]);
        exchangeRate.setRate(Double.parseDouble(row[2]));
        return exchangeRate;
    }
}

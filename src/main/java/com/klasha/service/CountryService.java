package com.klasha.service;

import java.util.List;
import java.util.Map;

public interface CountryService {
     Map<String, List<String>> getTopCities(int numberOfCities);

     Map<String,Object> getCountryDetails(String countryName);

     Map<String,List<String>> getStateAndCities(String countryName);

     Map<String,String> convertAmount(String countryName, String monetaryAmount, String targetCurrency);
}

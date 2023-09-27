package com.klasha.controller;

import com.klasha.service.CountryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/country")
public class CountryController {
    private final CountryService countryService;

    /**
     * Gets top N cities in Ghana, New zealand and italy filtered by population
     * @param N
     * @return List of top N cities in each country filtered by population
     */
    @GetMapping("/topCities")
    public ResponseEntity<Map<String, List<String>>> getTopCities(@RequestParam int N){
       return ResponseEntity.ok(countryService.getTopCities(N));
    }

    /**
     * Gets population,capital city, location, currency, ISO2&3 for specified country
     * @param countryName
     * @return details of the specified country in a map
     */
    @GetMapping("/details")
    public ResponseEntity<Map<String,Object>> getCountryDetails(@RequestParam String countryName){
        return ResponseEntity.ok(countryService.getCountryDetails(countryName));
    }

    /**
     * Gets all cities in each state in a country specified
     * @param countryName
     * @return List of cities in each state in a country
     */

    @GetMapping("/stateAndCities")
    public ResponseEntity<Map<String,List<String>>> getStateAndCities(@RequestParam String countryName){
        return ResponseEntity.ok(countryService.getStateAndCities(countryName));
    }

    /**
     * Calculate the amount for a specified monetary amount,country name and target currency
     * @param countryName
     * @param monetaryAmount
     * @param targetCurrency
     * @return currency and amount in the target currency
     */
    @GetMapping("/amount")
    public ResponseEntity<Map<String,String>> convertAmount(@RequestParam String countryName,
                                                                   @RequestParam String monetaryAmount,
                                                                   @RequestParam String targetCurrency){
        return ResponseEntity.ok(countryService.convertAmount(countryName,monetaryAmount,targetCurrency));
    }
}

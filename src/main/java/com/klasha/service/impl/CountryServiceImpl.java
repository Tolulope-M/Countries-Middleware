package com.klasha.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.klasha.constants.ExternalSourceUrl;
import com.klasha.dto.request.ExchangeRate;
import com.klasha.dto.request.PopulationFilterRequest;
import com.klasha.dto.response.*;
import com.klasha.exception.ClientSideException;
import com.klasha.exception.CustomException;
import com.klasha.exception.ServerSideException;
import com.klasha.service.CountryService;
import com.klasha.service.HttpClientService;
import com.klasha.utils.CsvReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.*;

import java.util.Currency;
import java.util.Locale;
import static com.klasha.constants.AppConstants.*;

@RequiredArgsConstructor
@Service
@Slf4j
public class CountryServiceImpl implements CountryService {
    private final ObjectMapper objectMapper;
    private final HttpClientService httpClientService;
    private final ExternalSourceUrl externalSourceUrl;
    @Override
    public Map<String,List<String> > getTopCities(int numberOfCities) {
        log.info(numberOfCities + " cities requested");
        Map<String,List<String>> populationResponse = new HashMap<>();
        try {
            //Get italy top N cites by population
            List<String> italyPopulationRes = initiatePopulationRequest(ITALY, numberOfCities);
            populationResponse.put(ITALY,italyPopulationRes);

            //Get New Zealand top N cites by population
            List<String> newZealandPopulationRes = initiatePopulationRequest(NEW_ZEALAND, numberOfCities);
            populationResponse.put(NEW_ZEALAND,newZealandPopulationRes);

            //Get Ghana top N cites by population
            List<String> ghanaPopulationRes = initiatePopulationRequest(GHANA, numberOfCities);
            populationResponse.put(GHANA,ghanaPopulationRes);

            return populationResponse;

        } catch (Exception e) {
            throw new CustomException(e.getMessage());
        }

    }
    private List<String> initiatePopulationRequest(String country, int numberOfCities){
        try {
            PopulationFilterRequest populationRequest = buildRequest(country);
            PopulationFilterResponse populationResponse = httpClientService.initiatePostRequest(PopulationFilterResponse.class,populationRequest,externalSourceUrl.getPopulationFilterUrl());
            return getData(populationResponse, numberOfCities);
        }
        catch (Exception e){
            throw new ServerSideException(e.getMessage());
        }
    }
    private List<String> getData(PopulationFilterResponse populationResponse, int numberOfCities){
        return populationResponse.getData().size() < numberOfCities ?
                populationResponse.getData()
                        .stream().map(PopulationFilterResponse.DataDto::getCity).toList()
                :populationResponse.getData().subList(0, numberOfCities)
                .stream().map(PopulationFilterResponse.DataDto::getCity).toList();
    }
    private PopulationFilterRequest buildRequest(String country) {
        return PopulationFilterRequest.builder()
                .country(country)
                .orderBy(POPULATION)
                .order(DESCENDING)
                .build();
    }
    @Override
    public Map<String,Object> getCountryDetails(String countryName) {
        Map<String,Object> responseMap = new HashMap<>();
        try {
            //Get population
            Map<String,String> request = new HashMap<>();
            request.put("country",countryName);
            Long population = getPopulation(request);
            responseMap.put("population",population);

            //Get Capital City, iso2 And iso3
            CapitalCityResponse capitalCityResponse = httpClientService.initiatePostRequest(CapitalCityResponse.class,request,externalSourceUrl.getCapitalCityUrl());
            String capital = capitalCityResponse.getData().getCapital();
            responseMap.put("capital",capital);
            //Get Location
            LocationResponse locationResponse1 = httpClientService.initiatePostRequest(LocationResponse.class,request,externalSourceUrl.getLocationUrl());
            Map<String,Integer> locationMap =new HashMap<>();
            locationMap.put("longitude",locationResponse1.getData().getLongitude());
            locationMap.put("latitude",locationResponse1.getData().getLat());
            responseMap.put("location",locationMap);

            //Get Currency
            CurrencyResponse currencyResponse1 = httpClientService.initiatePostRequest(CurrencyResponse.class,request,externalSourceUrl.getCurrencyUrl());
            String currency = currencyResponse1.getData().getCurrency();
            responseMap.put("currency",currency);

            //Get iso2 And iso3
            IsoResponse isoResponse1 = httpClientService.initiatePostRequest(IsoResponse.class,request,externalSourceUrl.getIsoUrl());
            responseMap.put("iso2",isoResponse1.getData().getIso2());
            responseMap.put("iso3",isoResponse1.getData().getIso3());

            return responseMap;
        }
        catch (Exception e){
            throw new ServerSideException(e.getMessage());
        }
    }
    private Long getPopulation(Map<String,String> request){
        PopulationResponse populationResponse = httpClientService.initiatePostRequest(PopulationResponse.class,request,externalSourceUrl.getPopulationUrl());
        Long population = populationResponse.getData().getPopulationCounts()
                .stream()
                .max(Comparator.comparingInt(PopulationResponse.Data.PopulationCount::getYear))
                .map(a -> a.getValue()).get();
        return population;
    }
    @Override
    public Map<String,List<String>> getStateAndCities(String countryName) {
        //Get States and Cities
        Map<String,List<String>> response = new HashMap<>();
        try {
            Map<String,String> request = new HashMap<>();
            request.put("country",countryName);
            StateResponse stateResponse = httpClientService.initiatePostRequest(StateResponse.class,request,externalSourceUrl.getStateUrl());

            stateResponse.getData().getStates().stream().map(StateResponse.Data.States::getName)
                    .forEach(state->{
                        request.put("state",state);
                        CityResponse cityResponse =httpClientService.initiatePostRequest(CityResponse.class,request,externalSourceUrl.getCityUrl());
                        response.put(state,cityResponse.getData());
                    });
            return response;
        }
        catch (Exception e){
            throw new ServerSideException(e.getMessage());
        }

    }

    @Override
    public Map<String,String> convertAmount(String countryName, String monetaryAmount, String targetCurrency) {
        Map<String,String> response = new HashMap<>();

        try {
            Map<String, String> request = new HashMap<>();
            request.put("country", countryName);
            CurrencyResponse currencyResponse1 = httpClientService.initiatePostRequest(CurrencyResponse.class,request,externalSourceUrl.getCurrencyUrl());
            String currency = currencyResponse1.getData().getCurrency();
            response.put("currency",currency);

            List<ExchangeRate> exchangeRates = CsvReader.readCSV(EXCHANGE_RATE_FILE_NAME);
            OptionalDouble tmpRate = exchangeRates.stream()
                    .filter(rate -> rate.getTargetCurrency().equalsIgnoreCase(targetCurrency) && rate.getSourceCurrency().equalsIgnoreCase(currency))
                    .mapToDouble(value -> value.getRate()).findFirst();
            if (tmpRate.isEmpty()) {
                throw new ClientSideException("Exchange rate does not exist with the given source and target currency");
            }
            double exchangeRate = tmpRate.getAsDouble();
            double totalAmount = Double.valueOf(monetaryAmount) * exchangeRate;
            DecimalFormat decimalFormat = new DecimalFormat("#,##0.00");
            Locale defaultLocale = Locale.getDefault();
            String symbol = Currency.getInstance(targetCurrency).getSymbol(defaultLocale);
            String formattedAmount = symbol+" "+decimalFormat.format(totalAmount);

            response.put("amount",formattedAmount);

            return response;
        }catch (Exception e){
            throw new ServerSideException(e.getMessage());
        }
    }
}

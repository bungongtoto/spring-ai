package com.llm.tool_calling.weather;

import com.llm.tool_calling.weather.dtos.WeatherRequest;
import com.llm.tool_calling.weather.dtos.WeatherResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestClient;

import java.util.function.Function;

/*
   Weather API
   https://www.weatherapi.com/api-explorer.aspx
 */
public class WeatherToolsFunction implements Function<WeatherRequest, WeatherResponse> {

    private static final Logger log = LoggerFactory.getLogger(WeatherToolsFunction.class);
    private final RestClient restClient;
    private final WeatherConfigProperties weatherProps;

    public WeatherToolsFunction(WeatherConfigProperties props) {
        this.weatherProps = props;
        log.debug("Weather API URL: {}", weatherProps.apiUrl());
        log.debug("Weather API Key: {}", weatherProps.apiKey());
        this.restClient = RestClient.create(weatherProps.apiUrl());
    }

    @Override
    public WeatherResponse apply(WeatherRequest weatherRequest) {
        log.info("Getting weather for location: {}", weatherRequest);
        try {
               WeatherResponse response = restClient
                        .get()
                        .uri("/current.json?key={key}&q={q}", weatherProps.apiKey(), weatherRequest.city())
                        .retrieve()
                        .body(WeatherResponse.class);

               log.info("Weather response: {}", response);
               return response;

        }catch (Exception e){
            log.error("Error getting weather for location: {}", weatherRequest, e);
            throw e;
        }
    }
}

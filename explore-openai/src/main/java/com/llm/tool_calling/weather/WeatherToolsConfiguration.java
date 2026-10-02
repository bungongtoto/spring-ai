package com.llm.tool_calling.weather;

import com.llm.tool_calling.weather.dtos.WeatherRequest;
import com.llm.tool_calling.weather.dtos.WeatherResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

import java.util.function.Function;

@Configuration(proxyBeanMethods = false)
public class WeatherToolsConfiguration {
    private final WeatherConfigProperties weatherProps;

    public  WeatherToolsConfiguration(WeatherConfigProperties weatherProps) {
        this.weatherProps = weatherProps;
    }

    @Bean
    @Description("Get current weather conditions for a given city.")
    public Function<WeatherRequest, WeatherResponse> currentWeatherFunction() {
        return new WeatherToolsFunction(this.weatherProps);
    }

}

package com.llm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class ExploreOpenaiApplication {

	public static void main(String[] args) {
		SpringApplication.run(ExploreOpenaiApplication.class, args);
	}

}

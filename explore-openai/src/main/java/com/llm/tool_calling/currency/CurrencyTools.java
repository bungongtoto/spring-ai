package com.llm.tool_calling.currency;

import com.llm.tool_calling.currency.dtos.CurrencyRequest;
import com.llm.tool_calling.currency.dtos.CurrencyResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class CurrencyTools {
    private static final Logger log = LoggerFactory.getLogger(CurrencyTools.class);

    private final RestClient restClient;
    private  final CurrencyExchangeConfigProperties currencyExchangeConfigProperties;

    public CurrencyTools(RestClient.Builder restClientBuilder, CurrencyExchangeConfigProperties currencyExchangeConfigProperties) {
        this.currencyExchangeConfigProperties = currencyExchangeConfigProperties;
        this.restClient = restClientBuilder.baseUrl(currencyExchangeConfigProperties.baseUrl()).build();
    }

    @Tool(description = "Get the latest currency exchange rates for a given base currency and symbols", returnDirect = true)
    public CurrencyResponse getLatestCurrencyRates(CurrencyRequest request, ToolContext toolContext) {
        log.info("Fetching latest currency rates for base: {} and symbols: {}", request.base(), request.symbols());

        if (toolContext != null) {
            var userId = toolContext.getContext().get("userId");

            log.info("UserId from tool context: {}", userId);
        }
        try {
            CurrencyResponse response = restClient
                    .get()
                    .uri("/latest.json?app_id={key}&base={base}&symbols={symbols}",currencyExchangeConfigProperties.apiKey(), request.base(), request.symbols())
                    .retrieve()
                    .body(CurrencyResponse.class);
            log.info("Currency response : {} ", response);
            return response;
        }catch (Exception e){
            log.error("Error fetching currency rates: {}", e.getMessage());
            throw e;
        }

    }

}
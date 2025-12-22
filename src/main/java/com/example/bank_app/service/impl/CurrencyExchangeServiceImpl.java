package com.example.bank_app.service.impl;

import com.example.bank_app.dto.external.ExchangeRateResponse;
import com.example.bank_app.service.CurrencyExchangeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CurrencyExchangeServiceImpl implements CurrencyExchangeService {

    private final WebClient webClient;
    @Value("${openexchangerates.api.key}")
    private String apiKey;

    @Override
    public BigDecimal convert(BigDecimal amount, String fromCurrency, String toCurrency) {
        if (fromCurrency.equals(toCurrency)) {
            return amount;
        }

        Map<String, BigDecimal> rates;

        if (fromCurrency.equals("USD")) {
            rates = getExchangeRateFromApi(fromCurrency, toCurrency);

            if (!rates.containsKey(toCurrency)) throw new RuntimeException("Tasa no encontrada para: " + toCurrency);

            return amount.multiply(rates.get(toCurrency)).setScale(4, RoundingMode.HALF_UP);
        }

        if (toCurrency.equals("USD")) {
            rates = getExchangeRateFromApi(toCurrency, fromCurrency);

            if (!rates.containsKey(fromCurrency)) throw new RuntimeException("Tasa no encontrada para: " + fromCurrency);

            return amount.divide(rates.get(fromCurrency), 4, RoundingMode.HALF_UP);
        }

        rates = getExchangeRateFromApi("USD", fromCurrency, toCurrency);

        if (!rates.containsKey(fromCurrency) || !rates.containsKey(toCurrency)) {
            throw new RuntimeException("Faltan tasas para realizar la conversión cruzada.");
        }

        return amount.multiply(rates.get(toCurrency)).divide(rates.get(fromCurrency), 4, RoundingMode.HALF_UP);
    }

    private Map<String, BigDecimal> getExchangeRateFromApi(String base, String symbol1, String symbol2) {
        return getExchangeRateFromApi(base, symbol1 + "," + symbol2);
    }

    private Map<String, BigDecimal> getExchangeRateFromApi(String from, String to) {
        log.info("Consultando WebClient: {} -> {}", from, to);

        ExchangeRateResponse response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/latest.json")
                        .queryParam("app_id", apiKey)
                        .queryParam("base", from)
                        .queryParam("symbols", to)
                        .build())
                .retrieve()
                .bodyToMono(ExchangeRateResponse.class)
                .block();


        if (response != null && response.rates() != null && !response.rates().isEmpty()) {
            return response.rates();
        }

        throw new RuntimeException("Error obteniendo el tipo de cambio de " + from + " a " + to);
    }
}
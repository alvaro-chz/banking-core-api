package com.example.bank_app.dto.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ExchangeRateResponse(
        String base,
        Map<String, BigDecimal> rates
) {}
package com.example.bank_app.service;

import java.math.BigDecimal;

public interface CurrencyExchangeService {
    BigDecimal convert(BigDecimal amount, String fromCurrency, String toCurrency);
}

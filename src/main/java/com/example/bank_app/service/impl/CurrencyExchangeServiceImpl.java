package com.example.bank_app.service.impl;

import com.example.bank_app.service.CurrencyExchangeService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Map;

@Service
public class CurrencyExchangeServiceImpl implements CurrencyExchangeService {
    // Tasas hardcodeadas respecto a 1 USD (Base)
    private static final Map<String, BigDecimal> EXCHANGE_RATES = Map.of(
            "USD", new BigDecimal("1.00"),
            "PEN", new BigDecimal("3.37"),  // 1 USD = 3.75 Soles
            "MXN", new BigDecimal("18.02")  // 1 USD = 18.02 Pesos
    );

    @Override
    public BigDecimal convert(BigDecimal amount, String fromCurrency, String toCurrency) {
        // 1. Si son la misma moneda, devolvemos el mismo monto
        if (fromCurrency.equals(toCurrency)) {
            return amount;
        }

        // 2. Validar soporte de monedas
        if (!EXCHANGE_RATES.containsKey(fromCurrency) || !EXCHANGE_RATES.containsKey(toCurrency)) {
            throw new RuntimeException("Moneda no soportada para conversión: " + fromCurrency + " -> " + toCurrency);
        }

        BigDecimal rateFrom = EXCHANGE_RATES.get(fromCurrency);
        BigDecimal rateTo = EXCHANGE_RATES.get(toCurrency);

        // 3. Lógica de Conversión (Pivote en USD)
        // Fórmula: (Monto / TasaOrigen) * TasaDestino

        // Paso A: Convertir a USD (Base)
        // MathContext.DECIMAL128 para evitar errores de división infinita (ej. 1/3)
        BigDecimal amountInUsd = amount.divide(rateFrom, MathContext.DECIMAL128);

        // Paso B: Convertir de USD a Moneda Destino
        BigDecimal finalAmount = amountInUsd.multiply(rateTo);

        // 4. Redondear a 4 decimales
        return finalAmount.setScale(4, RoundingMode.HALF_UP);
    }
}
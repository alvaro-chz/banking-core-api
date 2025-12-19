package com.example.bank_app.service.impl;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

class CurrencyExchangeServiceImplTest {
    private final CurrencyExchangeServiceImpl currencyExchangeService = new CurrencyExchangeServiceImpl();

    // --- Identity logic tests ---

    @Test
    void convert_SameCurrency() {
        // Given
        BigDecimal amount = new BigDecimal("100.00");

        // When
        BigDecimal result = currencyExchangeService.convert(amount, "USD", "USD");

        // Then
        assertThat(result).isEqualTo(amount);
    }

    @Test
    void convert_SameCurrency_PEN() {
        // Given
        BigDecimal amount = new BigDecimal("50.55");

        // When
        BigDecimal result = currencyExchangeService.convert(amount, "PEN", "PEN");

        // Then
        assertThat(result).isEqualTo(amount);
    }

    // --- Math conversion tests ---

    @Test
    void convert_UsdToPen() {
        // Given
        BigDecimal amount = new BigDecimal("10.00");

        // When
        BigDecimal result = currencyExchangeService.convert(amount, "USD", "PEN");

        // Then
        assertThat(result).isEqualByComparingTo("33.7000");
    }

    @Test
    void convert_PenToUsd() {
        // Given
        BigDecimal amount = new BigDecimal("10.00");

        // When
        BigDecimal result = currencyExchangeService.convert(amount, "PEN", "USD");

        // Then
        assertThat(result).isEqualByComparingTo("2.9674");
    }

    @Test
    void convert_MxnToPen() {
        // Given
        BigDecimal amount = new BigDecimal("100.00");

        // When
        BigDecimal result = currencyExchangeService.convert(amount, "MXN", "PEN");

        // Then
        assertThat(result).isEqualByComparingTo("18.7014");
    }

    // --- Error validation tests ---

    @Test
    void convert_UnsupportedSource() {
        // Given
        BigDecimal amount = BigDecimal.TEN;

        // When & then
        assertThatThrownBy(() -> currencyExchangeService.convert(amount, "EUR", "USD"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Moneda no soportada para conversión");
    }

    @Test
    void convert_UnsupportedTarget() {
        // Given
        BigDecimal amount = BigDecimal.TEN;

        // When & then
        assertThatThrownBy(() -> currencyExchangeService.convert(amount, "USD", "JPY"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Moneda no soportada para conversión: USD -> JPY");
    }
}
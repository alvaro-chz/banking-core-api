package com.example.bank_app.service.impl;

import com.example.bank_app.dto.external.ExchangeRateResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Collections;
import java.util.function.Function;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CurrencyExchangeServiceImplTest {
    @Mock private WebClient webClient;
    @Mock private WebClient.RequestHeadersUriSpec<?> requestHeadersUriSpec;
    @Mock private WebClient.RequestHeadersSpec<?> requestHeadersSpec;
    @Mock private WebClient.ResponseSpec responseSpec;

    @InjectMocks private CurrencyExchangeServiceImpl currencyExchangeService;

    @BeforeEach
    void setUp() {
        // API Key falsa para que no sea null
        ReflectionTestUtils.setField(currencyExchangeService, "apiKey", "test-api-key");
    }

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
        BigDecimal rate = new BigDecimal("3.37");
        mockWebClientCall(new ExchangeRateResponse("USD", Map.of("PEN", rate)));

        // When
        BigDecimal result = currencyExchangeService.convert(amount, "USD", "PEN");

        // Then
        assertThat(result).isEqualByComparingTo("33.7000");
    }

    @Test
    void convert_PenToUsd() {
        // Given
        BigDecimal amount = new BigDecimal("10.00");
        BigDecimal rate = new BigDecimal("3.37");
        mockWebClientCall(new ExchangeRateResponse("USD", Map.of("PEN", rate)));

        // When
        BigDecimal result = currencyExchangeService.convert(amount, "PEN", "USD");

        // Then
        assertThat(result).isEqualByComparingTo("2.9674");
    }

    @Test
    void convert_MxnToPen() {
        // Given
        BigDecimal amount = new BigDecimal("100.00");
        Map<String, BigDecimal> rates = Map.of(
                "MXN", new BigDecimal("18.00"),
                "PEN", new BigDecimal("3.60")
        );
        mockWebClientCall(new ExchangeRateResponse("USD", rates));

        // When
        BigDecimal result = currencyExchangeService.convert(amount, "MXN", "PEN");

        // Then
        assertThat(result).isEqualByComparingTo("20.0000");
    }

    // --- Error validation tests ---

    @Test
    void convert_UnsupportedSource() {
        // Given
        BigDecimal amount = BigDecimal.TEN;
        mockWebClientCall(new ExchangeRateResponse("USD", Collections.emptyMap()));

        // When & then
        assertThatThrownBy(() -> currencyExchangeService.convert(amount, "EUR", "USD"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Error obteniendo el tipo de cambio de USD a EUR");
    }

    @Test
    void convert_UnsupportedTarget() {
        // Given
        BigDecimal amount = BigDecimal.TEN;
        mockWebClientCall(new ExchangeRateResponse("USD", Map.of("PEN", BigDecimal.TEN)));

        // When & then
        assertThatThrownBy(() -> currencyExchangeService.convert(amount, "USD", "JPY"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Tasa no encontrada para: JPY");
    }

    // --- HELPER ---
    @SuppressWarnings({"unchecked", "rawtypes"})
    private void mockWebClientCall(ExchangeRateResponse mockResponse) {
        when(webClient.get()).thenReturn((WebClient.RequestHeadersUriSpec) requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(ExchangeRateResponse.class)).thenReturn(Mono.just(mockResponse));
    }
}
package com.example.bank_app.repository;

import com.example.bank_app.AbstractIntegrationTest;
import com.example.bank_app.model.Currency;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CurrencyRepositoryIT extends AbstractIntegrationTest {

    @Autowired
    private CurrencyRepository currencyRepository;

    // --- Basic CRUD Tests ---

    @Test
    void save_Success_And_FindById() {
        // Given
        Currency currency = Currency.builder()
                .id(1)
                .code("USD")
                .name("United States Dollar")
                .build();

        // When
        Currency saved = currencyRepository.save(currency);

        // Then
        Optional<Currency> retrieved = currencyRepository.findById(1);
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getCode()).isEqualTo("USD");
        assertThat(retrieved.get().getName()).isEqualTo("United States Dollar");
    }

    // --- Query Methods Tests ---

    @Test
    void findByCode_Success() {
        // Given
        currencyRepository.save(Currency.builder()
                .id(2)
                .code("EUR")
                .name("Euro")
                .build());

        // When
        Optional<Currency> result = currencyRepository.findByCode("EUR");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Euro");
    }

    @Test
    void findByCode_NotFound() {
        // Given

        // When
        Optional<Currency> result = currencyRepository.findByCode("JPY");

        // Then
        assertThat(result).isEmpty();
    }

    // --- DB Constraints Tests ---

    @Test
    void save_Fail_DuplicateCode() {
        // Given
        currencyRepository.saveAndFlush(Currency.builder()
                .id(3)
                .code("PEN")
                .name("Sol")
                .build());

        Currency duplicate = Currency.builder()
                .id(4)
                .code("PEN") // Duplicado
                .name("Nuevo Sol")
                .build();

        // When & Then
        assertThatThrownBy(() -> currencyRepository.saveAndFlush(duplicate))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
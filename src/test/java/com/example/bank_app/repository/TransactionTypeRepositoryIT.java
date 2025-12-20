package com.example.bank_app.repository;

import com.example.bank_app.AbstractIntegrationTest;
import com.example.bank_app.model.TransactionType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TransactionTypeRepositoryIT extends AbstractIntegrationTest {

    @Autowired
    private TransactionTypeRepository transactionTypeRepository;

    // --- Basic CRUD Tests ---

    @Test
    void save_Success_And_FindById() {
        // Given
        TransactionType type = TransactionType.builder()
                .id(10)
                .name("TRANSFER")
                .build();

        // When
        TransactionType saved = transactionTypeRepository.save(type);

        // Then
        Optional<TransactionType> retrieved = transactionTypeRepository.findById(10);
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getName()).isEqualTo("TRANSFER");
    }

    // --- DB Constraints Tests ---

    @Test
    void save_Fail_DuplicateName() {
        // Given
        transactionTypeRepository.saveAndFlush(TransactionType.builder()
                .id(20)
                .name("DEPOSIT")
                .build());

        TransactionType duplicate = TransactionType.builder()
                .id(21)
                .name("DEPOSIT")
                .build();

        // When & Then
        assertThatThrownBy(() -> transactionTypeRepository.saveAndFlush(duplicate))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void save_Fail_NullName() {
        // Given
        TransactionType invalidType = TransactionType.builder()
                .id(30)
                .name(null)
                .build();

        // When & Then
        assertThatThrownBy(() -> transactionTypeRepository.saveAndFlush(invalidType))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
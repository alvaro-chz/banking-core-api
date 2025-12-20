package com.example.bank_app.repository;

import com.example.bank_app.AbstractIntegrationTest;
import com.example.bank_app.model.TransactionStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TransactionStatusRepositoryIT extends AbstractIntegrationTest {

    @Autowired
    private TransactionStatusRepository transactionStatusRepository;

    // --- Basic CRUD Tests ---

    @Test
    void save_Success_And_FindById() {
        // Given
        TransactionStatus status = TransactionStatus.builder()
                .id(1)
                .name("PENDING")
                .build();

        // When
        TransactionStatus saved = transactionStatusRepository.save(status);

        // Then
        Optional<TransactionStatus> retrieved = transactionStatusRepository.findById(1);
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getName()).isEqualTo("PENDING");
    }

    // --- DB Constraints Tests ---

    @Test
    void save_Fail_DuplicateName() {
        // Given
        transactionStatusRepository.saveAndFlush(TransactionStatus.builder()
                .id(2)
                .name("COMPLETED")
                .build());

        TransactionStatus duplicate = TransactionStatus.builder()
                .id(3)
                .name("COMPLETED")
                .build();

        // When & Then
        assertThatThrownBy(() -> transactionStatusRepository.saveAndFlush(duplicate))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void save_Fail_NullName() {
        // Given
        TransactionStatus invalidStatus = TransactionStatus.builder()
                .id(4)
                .name(null)
                .build();

        // When & Then
        assertThatThrownBy(() -> transactionStatusRepository.saveAndFlush(invalidStatus))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
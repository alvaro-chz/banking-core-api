package com.example.bank_app.repository;

import com.example.bank_app.AbstractIntegrationTest;
import com.example.bank_app.model.AccountType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AccountTypeRepositoryIT extends AbstractIntegrationTest {

    @Autowired
    private AccountTypeRepository accountTypeRepository;

    // --- Basic CRUD Tests ---

    @Test
    void save_Success_And_FindById() {
        // Given
        AccountType type = AccountType.builder()
                .id(1)
                .name("CHECKING")
                .build();

        // When
        AccountType saved = accountTypeRepository.save(type);

        // Then
        Optional<AccountType> retrieved = accountTypeRepository.findById(1);
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getName()).isEqualTo("CHECKING");
    }

    // --- Query Methods Tests ---

    @Test
    void findByName_Success() {
        // Given
        accountTypeRepository.save(AccountType.builder()
                .id(2)
                .name("SAVINGS")
                .build());

        // When
        Optional<AccountType> result = accountTypeRepository.findByName("SAVINGS");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(2);
    }

    @Test
    void findByName_NotFound() {
        // Given

        // When
        Optional<AccountType> result = accountTypeRepository.findByName("INVESTMENT");

        // Then
        assertThat(result).isEmpty();
    }

    // --- DB Constraints Tests ---

    @Test
    void save_Fail_DuplicateName() {
        // Given
        accountTypeRepository.saveAndFlush(AccountType.builder()
                .id(3)
                .name("CREDIT")
                .build());

        AccountType duplicate = AccountType.builder()
                .id(4)
                .name("CREDIT")
                .build();

        // When & Then
        assertThatThrownBy(() -> accountTypeRepository.saveAndFlush(duplicate))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
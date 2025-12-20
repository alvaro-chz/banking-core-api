package com.example.bank_app.repository;

import com.example.bank_app.AbstractIntegrationTest;
import com.example.bank_app.model.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RoleRepositoryIT extends AbstractIntegrationTest {

    @Autowired
    private RoleRepository roleRepository;

    // --- Basic CRUD Tests ---

    @Test
    void save_Success_And_FindById() {
        // Given
        Role role = Role.builder()
                .id(100)
                .name("SUPER_ADMIN")
                .build();

        // When
        Role saved = roleRepository.save(role);

        // Then
        Optional<Role> retrieved = roleRepository.findById(100);
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getName()).isEqualTo("SUPER_ADMIN");
    }

    // --- Query Methods Tests ---

    @Test
    void findByName_Success() {
        // Given
        roleRepository.save(Role.builder()
                .id(101)
                .name("AUDITOR")
                .build());

        // When
        Optional<Role> result = roleRepository.findByName("AUDITOR");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(101);
    }

    @Test
    void findByName_NotFound() {
        // Given

        // When
        Optional<Role> result = roleRepository.findByName("NON_EXISTENT_ROLE");

        // Then
        assertThat(result).isEmpty();
    }

    // --- DB Constraints Tests ---

    @Test
    void save_Fail_DuplicateName() {
        // Given
        roleRepository.saveAndFlush(Role.builder()
                .id(102)
                .name("UNIQUE_ROLE")
                .build());

        Role duplicateRole = Role.builder()
                .id(103)
                .name("UNIQUE_ROLE") // Constraint violation
                .build();

        // When & Then
        assertThatThrownBy(() -> roleRepository.saveAndFlush(duplicateRole))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
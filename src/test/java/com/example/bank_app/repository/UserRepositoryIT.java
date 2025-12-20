package com.example.bank_app.repository;

import com.example.bank_app.AbstractIntegrationTest;
import com.example.bank_app.model.Role;
import com.example.bank_app.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserRepositoryIT extends AbstractIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    private Role defaultRole;

    @BeforeEach
    void setUp() {
        defaultRole = roleRepository.save(Role.builder()
                .id(2)
                .name("CLIENT")
                .build());
    }

    // --- Query methods tests ---

    @Test
    void findByEmail_Success() {
        // Given
        saveUser(
                "juan@mail.com",
                "11111111"
        );

        // When
        Optional<User> found = userRepository.findByEmail("juan@mail.com");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Test User");
        assertThat(found.get().getRole().getName()).isEqualTo("CLIENT");
    }

    @Test
    void existsByEmail_Check() {
        // Given
        saveUser(
                "exists@mail.com",
                "22222222"
        );

        // When & Then
        assertThat(userRepository.existsByEmail("exists@mail.com")).isTrue();
        assertThat(userRepository.existsByEmail("ghost@mail.com")).isFalse();
    }

    @Test
    void existsByDocumentId_Check() {
        // Given
        saveUser(
                "doc@mail.com",
                "87654321"
        );

        // When & Then
        assertThat(userRepository.existsByDocumentId("87654321")).isTrue();
        assertThat(userRepository.existsByDocumentId("00000000")).isFalse();
    }

    // --- DB Constraints tests ---

    @Test
    void save_Fail_DuplicateEmail() {
        // Given
        saveUser(
                "duplicate@mail.com",
                "11111111"
        );

        User duplicateUser = User.builder()
                .email("duplicate@mail.com")
                .documentId("99999999")
                .password("pass")
                .name("Other")
                .lastName1("User")
                .phoneNumber("999")
                .role(defaultRole)
                .build();

        // When & Then
        assertThatThrownBy(() -> userRepository.saveAndFlush(duplicateUser))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void save_Fail_DuplicateDocumentId() {
        // Given
        saveUser(
                "user1@mail.com",
                "88888888"
        );

        User duplicateUser = User.builder()
                .email("user2@mail.com")
                .documentId("88888888")
                .password("pass")
                .name("Other")
                .lastName1("User")
                .phoneNumber("999")
                .role(defaultRole)
                .build();

        // When & Then
        assertThatThrownBy(() -> userRepository.saveAndFlush(duplicateUser))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void save_AuditingCheck() {
        // Given
        User saved = saveUser(
                "audit@mail.com",
                "77777777"
        );

        // Then
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getIsActive()).isTrue();
    }

    // --- Helper Method ---
    private User saveUser(String email, String documentId) {
        User user = User.builder()
                .name("Test User")
                .lastName1("Lastname")
                .email(email)
                .documentId(documentId)
                .password("encodedPass")
                .phoneNumber("987654321")
                .role(defaultRole)
                .build();
        return userRepository.save(user);
    }
}
package com.example.bank_app.repository;

import com.example.bank_app.AbstractIntegrationTest;
import com.example.bank_app.model.LoginAttempt;
import com.example.bank_app.model.Role;
import com.example.bank_app.model.User;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LoginAttemptRepositoryIT extends AbstractIntegrationTest {

    @Autowired
    private LoginAttemptRepository loginAttemptRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private EntityManager entityManager;

    private User defaultUser;

    @BeforeEach
    void setUp() {
        Role clientRole = roleRepository.save(Role.builder()
                .id(2)
                .name("CLIENT")
                .build());

        defaultUser = userRepository.save(User.builder()
                .role(clientRole)
                .name("Test User")
                .lastName1("Lastname")
                .documentId("12345678")
                .email("test@mail.com")
                .password("encodedPass")
                .phoneNumber("987654321")
                .isActive(true)
                .build());
    }

    // --- Basic CRUD & Auditing tests ---

    @Test
    void save_Success_And_AuditingCheck() {
        // Given
        LoginAttempt attempt = LoginAttempt.builder()
                .user(defaultUser)
                .build();

        // When
        LoginAttempt saved = loginAttemptRepository.save(attempt);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getUser().getId()).isEqualTo(defaultUser.getId());
        assertThat(saved.getAttempts()).isEqualTo(0);
        assertThat(saved.getIsBlocked()).isFalse();
        assertThat(saved.getLastAttempt()).isNotNull();
    }

    @Test
    void update_ShouldUpdateAuditTimestamp() throws InterruptedException {
        // Given
        LoginAttempt attempt = loginAttemptRepository.save(LoginAttempt.builder()
                .user(defaultUser)
                .build());

        loginAttemptRepository.flush();
        LocalDateTime firstTimestamp = attempt.getLastAttempt();

        Thread.sleep(100);

        // When
        attempt.setAttempts(1);
        attempt.setIsBlocked(true);
        loginAttemptRepository.saveAndFlush(attempt);

        // Limpiamos la caché de Hibernate para obligar a leer el dato actualizado de la BD
        entityManager.clear();

        // Then
        Optional<LoginAttempt> updated = loginAttemptRepository.findById(attempt.getId());

        assertThat(updated).isPresent();
        assertThat(updated.get().getAttempts()).isEqualTo(1);
        assertThat(updated.get().getIsBlocked()).isTrue();
        assertThat(updated.get().getLastAttempt()).isAfter(firstTimestamp);
    }

    // --- DB Constraints tests ---

    @Test
    void save_Fail_DuplicateUser() {
        // Given
        loginAttemptRepository.saveAndFlush(LoginAttempt.builder()
                .user(defaultUser)
                .build());

        LoginAttempt duplicateAttempt = LoginAttempt.builder()
                .user(defaultUser)
                .attempts(5)
                .build();

        // When & Then
        assertThatThrownBy(() -> loginAttemptRepository.saveAndFlush(duplicateAttempt))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
package com.example.bank_app.repository;

import com.example.bank_app.AbstractIntegrationTest;
import com.example.bank_app.model.AuditLog;
import com.example.bank_app.model.Role;
import com.example.bank_app.model.User;
import com.example.bank_app.model.enums.AuditAction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuditLogRepositoryIT extends AbstractIntegrationTest {

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    private User defaultUser;

    @BeforeEach
    void setUp() {

        Role clientRole = roleRepository.save(Role.builder()
                .id(2)
                .name("CLIENT")
                .build());

        defaultUser = userRepository.save(User.builder()
                .role(clientRole)
                .name("Audit User")
                .lastName1("Tester")
                .documentId("87654321")
                .email("audit@test.com")
                .password("encodedPass")
                .phoneNumber("999888777")
                .isActive(true)
                .build());
    }

    // --- Happy Path Tests ---

    @Test
    void save_Success_And_GenerateTimestamp() {
        // Given
        AuditLog log = AuditLog.builder()
                .user(defaultUser)
                .action(AuditAction.LOGIN_SUCCESS)
                .description("User logged in successfully")
                .ipAddress("192.168.1.1")
                .build();

        // When
        AuditLog savedLog = auditLogRepository.save(log);

        // Then
        assertThat(savedLog.getId()).isNotNull();
        assertThat(savedLog.getCreatedAt()).isNotNull();
        assertThat(savedLog.getUser().getId()).isEqualTo(defaultUser.getId());
        assertThat(savedLog.getAction()).isEqualTo(AuditAction.LOGIN_SUCCESS);
    }

    // --- DB Constraints Tests ---

    @Test
    void save_Fail_NullAction() {
        // Given
        AuditLog invalidLog = AuditLog.builder()
                .user(defaultUser)
                .description("Action is missing")
                .action(null)
                .build();

        // When & Then
        assertThatThrownBy(() -> auditLogRepository.saveAndFlush(invalidLog))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void save_Success_WithoutOptionalFields() {
        // Given
        AuditLog minimalLog = AuditLog.builder()
                .user(defaultUser)
                .action(AuditAction.LOGIN_FAILED)
                .build();

        // When
        AuditLog savedLog = auditLogRepository.save(minimalLog);

        // Then
        assertThat(savedLog.getId()).isNotNull();
        assertThat(savedLog.getDescription()).isNull();
        assertThat(savedLog.getIpAddress()).isNull();
        assertThat(savedLog.getCreatedAt()).isNotNull();
    }
}
package com.example.bank_app.service.impl;

import com.example.bank_app.model.AuditLog;
import com.example.bank_app.model.User;
import com.example.bank_app.model.enums.AuditAction;
import com.example.bank_app.repository.AuditLogRepository;
import com.example.bank_app.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditLogServiceImplTest {

    @Mock private AuditLogRepository auditLogRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks private AuditLogServiceImpl auditLogService;

    @Test
    void logAction_ShouldCreateAndSaveAuditLog_WithFormattedDescription() {
        // Given
        Long userId = 1L;
        User userProxy = User.builder().id(userId).email("proxy@test.com").build();

        AuditAction action = AuditAction.LOGIN_SUCCESS;
        String description = "Inicio de sesión exitoso";
        String ipAddress = "192.168.1.100";
        String agent = "Mozilla/5.0 (Windows NT 10.0)";

        when(userRepository.getReferenceById(userId)).thenReturn(userProxy);

        // When
        auditLogService.logAction(userId, action, description, ipAddress, agent);

        // Then
        ArgumentCaptor<AuditLog> auditLogCaptor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(auditLogCaptor.capture());

        AuditLog capturedLog = auditLogCaptor.getValue();

        assertThat(capturedLog.getUser()).isEqualTo(userProxy);
        assertThat(capturedLog.getAction()).isEqualTo(action);
        assertThat(capturedLog.getIpAddress()).isEqualTo(ipAddress);
        assertThat(capturedLog.getDescription())
                .isEqualTo("Inicio de sesión exitoso| From: Mozilla/5.0 (Windows NT 10.0)");
    }
}
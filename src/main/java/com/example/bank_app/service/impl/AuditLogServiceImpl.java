package com.example.bank_app.service.impl;

import com.example.bank_app.model.AuditLog;
import com.example.bank_app.model.User;
import com.example.bank_app.model.enums.AuditAction;
import com.example.bank_app.repository.AuditLogRepository;
import com.example.bank_app.repository.UserRepository;
import com.example.bank_app.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {
    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    @Override
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logAction(Long userId, AuditAction action, String description, String ipAddress, String agent) {
        User user = userRepository.getReferenceById(userId);

        String finalDescription = description + "| From: " + agent;

        AuditLog log = AuditLog.builder()
                .user(user)
                .action(action)
                .description(finalDescription)
                .ipAddress(ipAddress)
                .build();

        auditLogRepository.save(log);
    }
}

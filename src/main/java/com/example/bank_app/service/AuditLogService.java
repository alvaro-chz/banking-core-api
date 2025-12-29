package com.example.bank_app.service;

import com.example.bank_app.model.User;
import com.example.bank_app.model.enums.AuditAction;

public interface AuditLogService {
    void logAction(Long userId, AuditAction action, String description, String ipAddress, String agent);
}

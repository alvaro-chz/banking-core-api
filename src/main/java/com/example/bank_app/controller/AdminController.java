package com.example.bank_app.controller;

import com.example.bank_app.dto.admin.AdminDashboardResponse;
import com.example.bank_app.dto.admin.UserAdminResponse;
import com.example.bank_app.dto.transaction.TransactionResponse;
import com.example.bank_app.model.User;
import com.example.bank_app.model.enums.AuditAction;
import com.example.bank_app.service.*;
import com.example.bank_app.util.WebUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin")
public class AdminController {
    private final TransactionService transactionService;
    private final AdminService adminService;
    private final UserService userService;
    private final LoginAttemptService loginAttemptService;
    private final AuditLogService auditLogService;

    @GetMapping("/dashboard")
    public ResponseEntity<AdminDashboardResponse> getDashboard() {
        return ResponseEntity.ok(adminService.getDashboard());
    }

    @GetMapping("/transactions")
    public ResponseEntity<Page<TransactionResponse>> getAllTransactions(
            @RequestParam(required = false) String accountNumber,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest request,
            Authentication auth
    ) {
        if (accountNumber != null && !accountNumber.isBlank()) {
            User admin = (User) auth.getPrincipal();
            auditLogService.logAction(
                    admin.getId(),
                    AuditAction.SEARCH_TRANSACTIONS,
                    "Admin buscó transacciones de la cuenta N°: " + accountNumber,
                    WebUtils.getClientIp(request),
                    WebUtils.getUserAgent(request)
            );
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        LocalDateTime startDateTime = (fromDate != null) ? fromDate.atStartOfDay() : null;
        LocalDateTime endDateTime = (toDate != null) ? toDate.atTime(LocalTime.MAX) : null;

        return ResponseEntity.ok(transactionService.getAllTransactions(
                accountNumber,
                status,
                startDateTime,
                endDateTime,
                pageable
        ));
    }

    @GetMapping("/users")
    public ResponseEntity<Page<UserAdminResponse>> getUsers(
            @RequestParam(required = false) String term,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) Boolean isBlocked,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest request,
            Authentication auth
    ) {
        if (term != null && !term.isBlank()) {
            User admin = (User) auth.getPrincipal();
            auditLogService.logAction(
                    admin.getId(),
                    AuditAction.SEARCH_USERS,
                    "Admin buscó usuarios con término: '" + term + "'",
                    WebUtils.getClientIp(request),
                    WebUtils.getUserAgent(request)
            );
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        return ResponseEntity.ok(userService.getUsers(term, isActive, isBlocked, pageable));
    }

    @PatchMapping("/users/{id}/unblock")
    public ResponseEntity<Void> unblockUser(
            @PathVariable Long id,
            HttpServletRequest request,
            Authentication auth
    ) {
        loginAttemptService.unblockUser(id);

        User admin = (User) auth.getPrincipal();
        auditLogService.logAction(
                admin.getId(),
                AuditAction.UNBLOCK_USER,
                "Admin desbloqueó al usuario ID: " + id,
                WebUtils.getClientIp(request),
                WebUtils.getUserAgent(request)
        );

        return ResponseEntity.ok().build();
    }
}

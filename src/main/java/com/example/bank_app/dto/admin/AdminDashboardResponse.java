package com.example.bank_app.dto.admin;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record AdminDashboardResponse(
        Long retainedUsersCount,
        Long totalBlockedUsersCount,
        List<BlockedUserSummary> lastUsersBlocked,
        Map<String, List<ChartDataPoint>> transactionCurve
) {
    public record BlockedUserSummary(
            String name,
            String documentId,
            LocalDateTime blockedAt
    ){}

    public record ChartDataPoint(
            LocalDate date,
            BigDecimal amount
    ){}
}

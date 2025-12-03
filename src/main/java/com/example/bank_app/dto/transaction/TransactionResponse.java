package com.example.bank_app.dto.transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionResponse(
    Integer id,
    String sourceAccount,
    String targetAccount,
    String transactionType,
    BigDecimal amount,
    String transactionStatus,
    String description,
    String referenceCode,
    String currency,
    LocalDateTime createdAt
) {
}

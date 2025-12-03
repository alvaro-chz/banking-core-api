package com.example.bank_app.dto.account;

import java.math.BigDecimal;

public record AccountResponse (
    Integer id,
    String currency,
    BigDecimal currentBalance,
    String accountType,
    String accountNumber
){
}

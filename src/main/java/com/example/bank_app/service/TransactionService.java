package com.example.bank_app.service;

import com.example.bank_app.dto.transaction.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface TransactionService {
    TransactionResponse transfer(TransferRequest request, Long userId);
    TransactionResponse deposit(DepositRequest request);
    TransactionResponse withdraw(WithdrawRequest request, Long userId);
    TransactionResponse payService(PayServiceRequest request, Long userId);
    TransactionResponse payInterest(PayInterestRequest request);

    Page<TransactionResponse> getHistory(
            String accountNumber,
            Long userId,
            String status,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable
    );

    Page<TransactionResponse> getAllTransactions(
            Long accountId,
            String status,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable
    );
}

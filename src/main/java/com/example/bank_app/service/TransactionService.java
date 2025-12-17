package com.example.bank_app.service;

import com.example.bank_app.dto.transaction.*;

import java.util.List;

public interface TransactionService {
    TransactionResponse transfer(TransferRequest request, Long userId);
    TransactionResponse deposit(DepositRequest request);
    TransactionResponse withdraw(WithdrawRequest request, Long userId);
    TransactionResponse payService(PayServiceRequest request, Long userId);
    TransactionResponse payInterest(PayInterestRequest request);

    List<TransactionResponse> getHistory(String accountNumber, Long userId);
}

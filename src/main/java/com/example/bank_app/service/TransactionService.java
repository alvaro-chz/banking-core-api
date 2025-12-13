package com.example.bank_app.service;

import com.example.bank_app.dto.transaction.*;

import java.util.List;

public interface TransactionService {
    TransactionResponse transfer(TransferRequest request, Integer userId);
    TransactionResponse deposit(DepositRequest request);
    TransactionResponse withdraw(WithdrawRequest request, Integer userId);
    TransactionResponse payService(PayServiceRequest request, Integer userId);
    TransactionResponse payInterest(PayInterestRequest request);

    List<TransactionResponse> getHistory(String accountNumber, Integer userId);
}

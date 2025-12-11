package com.example.bank_app.service;

import com.example.bank_app.dto.account.AccountCreationRequest;
import com.example.bank_app.dto.account.AccountResponse;

import java.util.List;

public interface AccountService {
    List<AccountResponse> getAccountsByUserId(Integer userId);
    AccountResponse createAccount(AccountCreationRequest request, Integer userId);
}

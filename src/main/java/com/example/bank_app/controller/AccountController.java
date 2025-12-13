package com.example.bank_app.controller;

import com.example.bank_app.dto.account.AccountCreationRequest;
import com.example.bank_app.dto.account.AccountResponse;
import com.example.bank_app.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
public class AccountController {
    private final AccountService accountService;

    @GetMapping("/user/{id}")
    public ResponseEntity<List<AccountResponse>> getAccounts(@PathVariable Integer id) {
        return ResponseEntity.ok(accountService.getAccountsByUserId(id));
    }

    @PostMapping("/user/{id}")
    public ResponseEntity<AccountResponse> create(@RequestBody @Valid AccountCreationRequest request, @PathVariable Integer id) {
        return new ResponseEntity<>(accountService.createAccount(request, id), HttpStatus.CREATED);
    }
}

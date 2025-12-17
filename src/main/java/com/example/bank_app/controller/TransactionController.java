package com.example.bank_app.controller;

import com.example.bank_app.dto.transaction.*;
import com.example.bank_app.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/transactions")
public class TransactionController {
    private final TransactionService transactionService;

    @PostMapping("/user/{id}/transfer")
    public ResponseEntity<TransactionResponse> transfer(@RequestBody @Valid TransferRequest request, @PathVariable Long id) {
        return new ResponseEntity<>(transactionService.transfer(request, id), HttpStatus.CREATED);
    }

    @PostMapping("/deposit")
    public ResponseEntity<TransactionResponse> deposit(@RequestBody @Valid DepositRequest request) {
        return new ResponseEntity<>(transactionService.deposit(request), HttpStatus.CREATED);
    }

    @PostMapping("/user/{id}/withdraw")
    public ResponseEntity<TransactionResponse> withdraw(@RequestBody @Valid WithdrawRequest request, @PathVariable Long id) {
        return new ResponseEntity<>(transactionService.withdraw(request, id), HttpStatus.CREATED);
    }

    @PostMapping("/user/{id}/payment")
    public ResponseEntity<TransactionResponse> payService(@RequestBody @Valid PayServiceRequest request, @PathVariable Long id) {
        return new ResponseEntity<>(transactionService.payService(request, id), HttpStatus.CREATED);
    }

    @GetMapping("/history/account/{accountNumber}")
    public ResponseEntity<List<TransactionResponse>> getHistory(@PathVariable String accountNumber, @RequestParam Long userId) {
        return ResponseEntity.ok(transactionService.getHistory(accountNumber, userId));
    }
}

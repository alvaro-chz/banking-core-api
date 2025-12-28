package com.example.bank_app.controller;

import com.example.bank_app.dto.transaction.*;
import com.example.bank_app.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
    public ResponseEntity<Page<TransactionResponse>> getHistory(
            @PathVariable String accountNumber,
            @RequestParam Long userId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        // Lógica de Fechas (Conversión de Día a Rango de Tiempo)
        LocalDateTime startDateTime = (fromDate != null) ? fromDate.atStartOfDay() : null;
        LocalDateTime endDateTime = (toDate != null) ? toDate.atTime(LocalTime.MAX) : null;

        return ResponseEntity.ok(transactionService.getHistory(
                accountNumber,
                userId,
                status,
                startDateTime,
                endDateTime,
                pageable
        ));
    }
}

package com.example.bank_app.repository;

import com.example.bank_app.model.BankTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BankTransactionRepository extends JpaRepository <BankTransaction, Integer> {
}

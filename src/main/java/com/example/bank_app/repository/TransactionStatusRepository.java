package com.example.bank_app.repository;

import com.example.bank_app.model.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionStatusRepository extends JpaRepository<TransactionStatus, Integer> {
}

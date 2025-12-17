package com.example.bank_app.repository;

import com.example.bank_app.model.BankTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BankTransactionRepository extends JpaRepository <BankTransaction, Long> {
    boolean existsByReferenceCode(String referenceCode);

    @Query("SELECT t FROM BankTransaction t WHERE t.sourceAccount.id = :accountId OR t.targetAccount.id = :accountId ORDER BY t.createdAt DESC")
    // Equivale a:
    // @Query(value = "SELECT * FROM bank_transaction WHERE source_account_id = :accountId OR target_account_id = :accountId ORDER BY created_at DESC", nativeQuery = true)
    List<BankTransaction> findAllByAccountId(@Param("accountId") Long accountId);
}

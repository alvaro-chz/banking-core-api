package com.example.bank_app.repository;

import com.example.bank_app.model.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface BankAccountRepository extends JpaRepository<BankAccount, Integer> {
    List<BankAccount> findAllByUserIdAndIsActiveTrue(Integer userId);
    boolean existsByAccountNumber(String accountNumber);
    boolean existsByAccountNumberAndUserId(String accountNumber, Integer userId);
    Optional<BankAccount> findByAccountNumberAndIsActiveTrue(String accountNumber);
}

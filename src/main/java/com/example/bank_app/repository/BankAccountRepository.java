package com.example.bank_app.repository;

import com.example.bank_app.model.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BankAccountRepository extends JpaRepository<BankAccount, Integer> {
    List<BankAccount> findAllByUserId(Integer userId);
    boolean existsByAccountNumber(String accountNumber);
}

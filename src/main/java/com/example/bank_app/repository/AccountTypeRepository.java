package com.example.bank_app.repository;

import com.example.bank_app.model.AccountType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AccountTypeRepository extends JpaRepository<AccountType, Integer> {
    Optional<AccountType> findByName(String name);
}

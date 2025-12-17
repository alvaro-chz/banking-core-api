package com.example.bank_app.repository;

import com.example.bank_app.model.Beneficiary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BeneficiaryRepository extends JpaRepository<Beneficiary, Long> {
    List<Beneficiary> findAllByUserIdAndIsActiveTrue(Long userId);
    boolean existsByUserIdAndAccountNumberAndIsActiveTrue(Long userId, String accountNumber);
}

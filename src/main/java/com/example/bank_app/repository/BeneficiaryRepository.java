package com.example.bank_app.repository;

import com.example.bank_app.model.Beneficiary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BeneficiaryRepository extends JpaRepository<Beneficiary, Integer> {
    List<Beneficiary> findAllByUserIdAndIsActiveTrue(Integer userId);
    boolean existsByUserIdAndAccountNumberAndIsActiveTrue(Integer userId, String accountNumber);
}

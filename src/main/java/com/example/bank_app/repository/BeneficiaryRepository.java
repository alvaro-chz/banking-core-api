package com.example.bank_app.repository;

import com.example.bank_app.dto.beneficiary.BeneficiaryResponse;
import com.example.bank_app.model.Beneficiary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BeneficiaryRepository extends JpaRepository<Beneficiary, Integer> {
    List<Beneficiary> findAllByUserIdAndIsActiveTrue(Integer userId);
    boolean existsByUserIdAndAccountNumberAndIsActiveTrue(Integer userId, String accountNumber);
}

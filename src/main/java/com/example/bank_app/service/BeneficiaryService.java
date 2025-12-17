package com.example.bank_app.service;

import com.example.bank_app.dto.beneficiary.BeneficiaryCreateRequest;
import com.example.bank_app.dto.beneficiary.BeneficiaryResponse;
import com.example.bank_app.dto.beneficiary.BeneficiaryUpdateRequest;

import java.util.List;

public interface BeneficiaryService {
    List<BeneficiaryResponse> getBeneficiaries(Long userId);
    BeneficiaryResponse addBeneficiary(BeneficiaryCreateRequest request, Long userId);
    BeneficiaryResponse updateBeneficiary(BeneficiaryUpdateRequest request, Long beneficiaryId, Long userId);
    void deleteBeneficiary (Long beneficiaryId, Long userId);
}

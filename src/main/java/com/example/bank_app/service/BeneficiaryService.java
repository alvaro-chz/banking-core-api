package com.example.bank_app.service;

import com.example.bank_app.dto.beneficiary.BeneficiaryCreateRequest;
import com.example.bank_app.dto.beneficiary.BeneficiaryResponse;
import com.example.bank_app.dto.beneficiary.BeneficiaryUpdateRequest;

import java.util.List;

public interface BeneficiaryService {
    List<BeneficiaryResponse> getBeneficiaries(Integer userId);
    BeneficiaryResponse addBeneficiary(BeneficiaryCreateRequest request, Integer userId);
    BeneficiaryResponse updateBeneficiary(BeneficiaryUpdateRequest request, Integer beneficiaryId, Integer userId);
    void deleteBeneficiary (Integer beneficiaryId, Integer userId);
}

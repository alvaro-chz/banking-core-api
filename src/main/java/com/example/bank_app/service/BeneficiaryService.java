package com.example.bank_app.service;

import com.example.bank_app.dto.beneficiary.BeneficiaryRequest;
import com.example.bank_app.dto.beneficiary.BeneficiaryResponse;

import java.util.List;

public interface BeneficiaryService {
    List<BeneficiaryResponse> getBeneficiaries(Integer userId);
    BeneficiaryResponse addBeneficiary(BeneficiaryRequest request, Integer userId);
    BeneficiaryResponse updateBeneficiary(BeneficiaryRequest request, Integer beneficiaryId, Integer userId);
    void deleteBeneficiary (Integer beneficiaryId, Integer userId);
}

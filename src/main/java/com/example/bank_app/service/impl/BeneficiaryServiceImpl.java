package com.example.bank_app.service.impl;

import com.example.bank_app.dto.beneficiary.BeneficiaryRequest;
import com.example.bank_app.dto.beneficiary.BeneficiaryResponse;
import com.example.bank_app.model.Beneficiary;
import com.example.bank_app.model.User;
import com.example.bank_app.repository.BankAccountRepository;
import com.example.bank_app.repository.BeneficiaryRepository;
import com.example.bank_app.repository.UserRepository;
import com.example.bank_app.service.BeneficiaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class BeneficiaryServiceImpl implements BeneficiaryService {
    private final BeneficiaryRepository beneficiaryRepository;
    private final UserRepository userRepository;
    private final BankAccountRepository bankAccountRepository;

    @Override
    @Transactional(readOnly = true)
    public List<BeneficiaryResponse> getBeneficiaries(Integer userId) {
        return beneficiaryRepository.findAllByUserIdAndIsActiveTrue(userId)
                .stream()
                .map(this::mapToBeneficiaryResponse)
                .toList();
    }

    @Override
    public BeneficiaryResponse addBeneficiary(BeneficiaryRequest request, Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario con ID: " + userId + " no encontrado"));

        validateBeneficiaryRules(userId, request.accountNumber());

        Beneficiary beneficiary = Beneficiary.builder()
                .user(user)
                .accountNumber(request.accountNumber())
                .bankName(request.bankName())
                .alias(request.alias())
                .build();

        Beneficiary saved = beneficiaryRepository.save(beneficiary);

        return mapToBeneficiaryResponse(saved);
    }

    @Override
    public void deleteBeneficiary(Integer beneficiaryId, Integer userId) {
        Beneficiary beneficiary = getBeneficiaryAndValidateOwnership(beneficiaryId, userId);

        beneficiary.setIsActive(false);
    }

    @Override
    public BeneficiaryResponse updateBeneficiary(BeneficiaryRequest request, Integer beneficiaryId, Integer userId) {
        Beneficiary beneficiary = getBeneficiaryAndValidateOwnership(beneficiaryId, userId);

        if (request.accountNumber() != null && !request.accountNumber().isBlank()) {
            // Aunque JPA lo gestiona, este if es para evitar falsas excepciones al editar otro
            // valor (Por usar validateBeneficiaryRules).
            if (!request.accountNumber().equals(beneficiary.getAccountNumber())) {
                validateBeneficiaryRules(userId, request.accountNumber());

                beneficiary.setAccountNumber(request.accountNumber());
            }
        }

        if (request.bankName() != null && !request.bankName().isBlank()) {
            if (!request.bankName().equals(beneficiary.getBankName())) {
                beneficiary.setBankName(request.bankName());
            }
        }

        if (request.alias() != null && !request.alias().isBlank()) {
            if (!request.alias().equals(beneficiary.getAlias())) {
                beneficiary.setAlias(request.alias());
            }
        }

        return mapToBeneficiaryResponse(beneficiary);
    }

    // Métodos privados

    private Beneficiary getBeneficiaryAndValidateOwnership(Integer beneficiaryId, Integer userId) {
        Beneficiary beneficiary = beneficiaryRepository.findById(beneficiaryId)
                .orElseThrow(() -> new RuntimeException("Beneficiario no encontrado"));

        if (!beneficiary.getUser().getId().equals(userId)) {
            throw new RuntimeException("Beneficiario no encontrado o acceso denegado");
        }

        if (!Boolean.TRUE.equals(beneficiary.getIsActive())) {
            throw new RuntimeException("El beneficiario no se encuentra disponible");
        }

        return beneficiary;
    }

    private void validateBeneficiaryRules(Integer userId, String accountNumber) {
        if (beneficiaryRepository.existsByUserIdAndAccountNumberAndIsActiveTrue(userId, accountNumber)) {
            throw new RuntimeException("Esta cuenta ya está agregada a tus beneficiarios.");
        }

        boolean isOwnAccount = bankAccountRepository.existsByAccountNumberAndUserId(accountNumber, userId);
        if (isOwnAccount) {
            throw new RuntimeException("No te puedes agregar a ti mismo como beneficiario.");
        }
    }

    private BeneficiaryResponse mapToBeneficiaryResponse(Beneficiary beneficiary) {
        return new BeneficiaryResponse(
                beneficiary.getId(),
                beneficiary.getAlias(),
                beneficiary.getAccountNumber(),
                beneficiary.getBankName()
        );
    }
}

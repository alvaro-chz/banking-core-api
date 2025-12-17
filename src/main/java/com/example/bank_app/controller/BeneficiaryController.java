package com.example.bank_app.controller;

import com.example.bank_app.dto.beneficiary.BeneficiaryCreateRequest;
import com.example.bank_app.dto.beneficiary.BeneficiaryResponse;
import com.example.bank_app.dto.beneficiary.BeneficiaryUpdateRequest;
import com.example.bank_app.service.BeneficiaryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/beneficiaries")
public class BeneficiaryController {
    private final BeneficiaryService beneficiaryService;

    @GetMapping("/user/{id}")
    public ResponseEntity<List<BeneficiaryResponse>> getAll(@PathVariable Long id) {
        return ResponseEntity.ok(beneficiaryService.getBeneficiaries(id));
    }

    @PostMapping("/user/{id}")
    public ResponseEntity<BeneficiaryResponse> add(@RequestBody @Valid BeneficiaryCreateRequest request, @PathVariable Long id) {
        return new ResponseEntity<>(beneficiaryService.addBeneficiary(request, id), HttpStatus.CREATED);
    }

    @PutMapping("/user/{userId}/{id}")
    public ResponseEntity<BeneficiaryResponse> update(@RequestBody @Valid BeneficiaryUpdateRequest request, @PathVariable Long id, @PathVariable Long userId) {
        return ResponseEntity.ok(beneficiaryService.updateBeneficiary(request, id, userId));
    }

    @DeleteMapping("/user/{userId}/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, @PathVariable Long userId) {
        beneficiaryService.deleteBeneficiary(id, userId);
        return ResponseEntity.ok().build();
    }
}

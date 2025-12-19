package com.example.bank_app.service.impl;

import com.example.bank_app.dto.beneficiary.BeneficiaryCreateRequest;
import com.example.bank_app.dto.beneficiary.BeneficiaryResponse;
import com.example.bank_app.dto.beneficiary.BeneficiaryUpdateRequest;
import com.example.bank_app.model.Beneficiary;
import com.example.bank_app.model.User;
import com.example.bank_app.repository.BankAccountRepository;
import com.example.bank_app.repository.BeneficiaryRepository;
import com.example.bank_app.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BeneficiaryServiceImplTest {

    @Mock private BeneficiaryRepository beneficiaryRepository;
    @Mock private UserRepository userRepository;
    @Mock private BankAccountRepository bankAccountRepository;

    @InjectMocks private BeneficiaryServiceImpl beneficiaryService;

    // --- GetBeneficiaries Test ---

    @Test
    void getBeneficiariesSuccess() {
        // Given
        Long userId = 1L;
        Beneficiary beneficiary = Beneficiary.builder()
                .id(100L)
                .alias("Mi Amigo")
                .accountNumber("123456")
                .isActive(true)
                .build();

        when(beneficiaryRepository.findAllByUserIdAndIsActiveTrue(userId))
                .thenReturn(List.of(beneficiary));

        // When
        List<BeneficiaryResponse> result = beneficiaryService.getBeneficiaries(userId);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).alias()).isEqualTo("Mi Amigo");
        verify(beneficiaryRepository).findAllByUserIdAndIsActiveTrue(userId);
    }

    // --- AddBeneficiary Test ---

    @Test
    void addBeneficiarySuccess() {
        // Given
        Long userId = 1L;
        User user = User.builder().id(userId).build();
        BeneficiaryCreateRequest request = new BeneficiaryCreateRequest(
                "12345",
                "Banco X",
                "Alias Test"
        );

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));
        when(beneficiaryRepository.existsByUserIdAndAccountNumberAndIsActiveTrue(userId, request.accountNumber()))
                .thenReturn(false); // No existe previamente
        when(bankAccountRepository.existsByAccountNumberAndUserId(request.accountNumber(), userId))
                .thenReturn(false); // No es cuenta propia

        when(beneficiaryRepository.save(any(Beneficiary.class)))
                .thenAnswer(invocation -> {
                    Beneficiary b = invocation.getArgument(0);
                    b.setId(999L);
                    return b;
                });

        // When
        BeneficiaryResponse response = beneficiaryService.addBeneficiary(request, userId);

        // Then
        assertThat(response.id()).isEqualTo(999L);
        assertThat(response.alias()).isEqualTo("Alias Test");
        verify(beneficiaryRepository).save(any(Beneficiary.class));
    }

    // --- UpdateBeneficiary Test ---

    @Test
    void updateBeneficiarySuccess() {
        // Given
        Long userId = 1L;
        Long beneficiaryId = 10L;
        User user = User.builder().id(userId).build();

        Beneficiary existingBeneficiary = Beneficiary.builder()
                .id(beneficiaryId)
                .user(user)
                .accountNumber("OLD-ACCOUNT")
                .alias("Old Alias")
                .isActive(true)
                .build();

        BeneficiaryUpdateRequest request = new BeneficiaryUpdateRequest(
                "NEW-ACCOUNT",
                "New Bank",
                "New Alias"
        );

        when(beneficiaryRepository.findById(beneficiaryId))
                .thenReturn(Optional.of(existingBeneficiary));
        when(beneficiaryRepository.existsByUserIdAndAccountNumberAndIsActiveTrue(userId, "NEW-ACCOUNT"))
                .thenReturn(false);
        when(bankAccountRepository.existsByAccountNumberAndUserId("NEW-ACCOUNT", userId))
                .thenReturn(false);

        // When
        BeneficiaryResponse response = beneficiaryService.updateBeneficiary(request, beneficiaryId, userId);

        // Then
        assertThat(response.accountNumber()).isEqualTo("NEW-ACCOUNT");
        assertThat(response.alias()).isEqualTo("New Alias");
        assertThat(existingBeneficiary.getAccountNumber()).isEqualTo("NEW-ACCOUNT");
    }

    // --- DeleteBeneficiary Test ---

    @Test
    void deleteBeneficiarySuccess() {
        // Given
        Long userId = 1L;
        Long beneficiaryId = 10L;
        User user = User.builder().id(userId).build();

        Beneficiary beneficiary = Beneficiary.builder()
                .id(beneficiaryId)
                .user(user)
                .isActive(true)
                .build();

        when(beneficiaryRepository.findById(beneficiaryId))
                .thenReturn(Optional.of(beneficiary));

        // When
        beneficiaryService.deleteBeneficiary(beneficiaryId, userId);

        // Then
        assertThat(beneficiary.getIsActive()).isFalse();
    }

    // --- Tests para cubrir getBeneficiaryAndValidateOwnership (vía deleteBeneficiary) ---

    @Test
    void deleteBeneficiary_Fail_NotOwner() {
        // Given
        Long myUserId = 1L;
        Long otherUserId = 2L;
        Long beneficiaryId = 10L;

        User otherUser = User.builder().id(otherUserId).build();
        Beneficiary beneficiary = Beneficiary.builder()
                .id(beneficiaryId)
                .user(otherUser)
                .isActive(true)
                .build();

        when(beneficiaryRepository.findById(beneficiaryId))
                .thenReturn(Optional.of(beneficiary));

        // When & Then
        assertThatThrownBy(() -> beneficiaryService.deleteBeneficiary(beneficiaryId, myUserId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Beneficiario no encontrado o acceso denegado");
    }

    @Test
    void deleteBeneficiary_Fail_Inactive() {
        // Given
        Long userId = 1L;
        Long beneficiaryId = 10L;
        User user = User.builder().id(userId).build();

        Beneficiary beneficiary = Beneficiary.builder()
                .id(beneficiaryId)
                .user(user)
                .isActive(false)
                .build();

        when(beneficiaryRepository.findById(beneficiaryId))
                .thenReturn(Optional.of(beneficiary));

        // When & Then
        assertThatThrownBy(() -> beneficiaryService.deleteBeneficiary(beneficiaryId, userId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("El beneficiario no se encuentra disponible");
    }

    // --- Tests para cubrir validateBeneficiaryRules (vía addBeneficiary) ---

    @Test
    void addBeneficiary_Fail_AlreadyExists() {
        // Given
        Long userId = 1L;
        User user = User.builder().id(userId).build();
        BeneficiaryCreateRequest request = new BeneficiaryCreateRequest(
                "12345",
                "Banco X",
                "Alias Test"
        );

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));
        when(beneficiaryRepository.existsByUserIdAndAccountNumberAndIsActiveTrue(userId, request.accountNumber()))
                .thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> beneficiaryService.addBeneficiary(request, userId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Esta cuenta ya está agregada a tus beneficiarios.");
        verify(beneficiaryRepository, never()).save(any());
    }

    @Test
    void addBeneficiary_Fail_OwnAccount() {
        // Given
        Long userId = 1L;
        User user = User.builder().id(userId).build();
        BeneficiaryCreateRequest request = new BeneficiaryCreateRequest(
                "12345",
                "Banco X",
                "Alias Test"
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(beneficiaryRepository.existsByUserIdAndAccountNumberAndIsActiveTrue(userId, request.accountNumber()))
                .thenReturn(false);
        when(bankAccountRepository.existsByAccountNumberAndUserId(request.accountNumber(), userId))
                .thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> beneficiaryService.addBeneficiary(request, userId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("No te puedes agregar a ti mismo como beneficiario.");
    }
}
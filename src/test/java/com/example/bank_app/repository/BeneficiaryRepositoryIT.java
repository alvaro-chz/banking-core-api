package com.example.bank_app.repository;

import com.example.bank_app.AbstractIntegrationTest;
import com.example.bank_app.model.Beneficiary;
import com.example.bank_app.model.Role;
import com.example.bank_app.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

public class BeneficiaryRepositoryIT extends AbstractIntegrationTest {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private BeneficiaryRepository beneficiaryRepository;

    private Role defaultRole;
    private User userTest;

    @BeforeEach
    void setUp() {
        defaultRole = roleRepository.save(Role.builder()
                .id(2)
                .name("CLIENT")
                .build());

        userTest = userRepository.save(User.builder()
                .role(defaultRole)
                .name("Test user")
                .lastName1("lastname")
                .documentId("1234567")
                .email("test@email.com")
                .password("encodedPass")
                .phoneNumber("987654321")
                .build());
    }

    // --- Query methods tests ---

    @Test
    @Transactional
    void findAllByUserIdAndIsActiveTrue_Success() {
        // Given
        saveBeneficiary("777666555");
        Long userId = userTest.getId();

        // When
        List<Beneficiary> found = beneficiaryRepository.findAllByUserIdAndIsActiveTrue(userId);

        // Then
        assertThat(found).hasSize(1);

        Beneficiary response = found.get(0);
        assertThat(response.getUser().getEmail()).isEqualTo("test@email.com");
        assertThat(response.getUser().getRole().getName()).isEqualTo("CLIENT");
        assertThat(response.getAccountNumber()).isEqualTo("777666555");
    }

    @Test
    @Transactional
    void findAll_ShouldIgnoreInactiveBeneficiaries() {
        // Given
        saveBeneficiary("111111");
        Beneficiary inactive = Beneficiary.builder()
                .user(userTest)
                .alias("Antiguo amigo")
                .accountNumber("000000")
                .isActive(false) // <--- CLAVE
                .build();

        beneficiaryRepository.save(inactive);
        Long userId = userTest.getId();

        // When
        List<Beneficiary> found = beneficiaryRepository.findAllByUserIdAndIsActiveTrue(userId);

        // Then
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getAccountNumber()).isEqualTo("111111");

    }

    @Test
    void existsByUserIdAndAccountNumberAndIsActiveTrue_Check() {
        // Given
        saveBeneficiary("777666555");
        Beneficiary inactive = Beneficiary.builder()
                .user(userTest)
                .alias("Inactivo")
                .accountNumber("000000")
                .isActive(false)
                .build();

        beneficiaryRepository.save(inactive);
        Long userId = userTest.getId();

        // When & Then
        assertThat(beneficiaryRepository.existsByUserIdAndAccountNumberAndIsActiveTrue(userId, "777666555"))
                .isTrue();
        assertThat(beneficiaryRepository.existsByUserIdAndAccountNumberAndIsActiveTrue(2L, "777666555"))
                .isFalse();
        assertThat(beneficiaryRepository.existsByUserIdAndAccountNumberAndIsActiveTrue(userId,"7"))
                .isFalse();
        assertThat(beneficiaryRepository.existsByUserIdAndAccountNumberAndIsActiveTrue(userId, "000000"))
                .isFalse();
    }

    // --- DB Constraints tests ---
    @Test
    void save_DefaultValuesCheck() {
        // Given
        Beneficiary beneficiary = saveBeneficiary("777666555");

        // Then
        assertThat(beneficiary.getIsActive()).isTrue();
    }


    // --- Helper method ---
    private Beneficiary saveBeneficiary(String accountNumber) {
        return beneficiaryRepository.save(
                Beneficiary.builder()
                        .user(userTest)
                        .alias("Test alias")
                        .accountNumber(accountNumber)
                        .build()
        );
    }
}

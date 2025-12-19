package com.example.bank_app.service.impl;

import com.example.bank_app.dto.user.ChangePasswordRequest;
import com.example.bank_app.dto.user.UserResponse;
import com.example.bank_app.dto.user.UserUpdateRequest;
import com.example.bank_app.model.Role;
import com.example.bank_app.model.User;
import com.example.bank_app.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks private UserServiceImpl userService;

    private User user;

    @BeforeEach
    void setUp() {
        Role roleUser = Role.builder().id(1).name("CLIENTE").build();
        user = User.builder()
                .id(1L)
                .name("Juan")
                .lastName1("Perez")
                .lastName2("Gomez")
                .documentId("12345678")
                .email("juan@test.com")
                .phoneNumber("999999999")
                .password("ENCODED_PASSWORD") // Simulamos que ya está encriptada en BD
                .role(roleUser)
                .createdAt(LocalDateTime.now())
                .build();
    }

    // --- GetUserProfile test ---

    @Test
    void getUserProfileSuccess() {
        // Given
        Long userId = 1L;
        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        // When
        UserResponse response = userService.getUserProfile(userId);

        // Then
        assertThat(response.email()).isEqualTo("juan@test.com");
        assertThat(response.role()).isEqualTo("CLIENTE");
        assertThat(response.name()).isEqualTo("Juan");
    }

    // --- UpdateUser test ---

    @Test
    void updateUserSuccess() {
        // Given
        Long userId = 1L;
        UserUpdateRequest request = new UserUpdateRequest(
                "juan.new@test.com",
                "987654321"
        );

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("juan.new@test.com"))
                .thenReturn(false);

        // When
        UserResponse response = userService.updateUser(request, userId);

        // Then
        assertThat(response.email()).isEqualTo("juan.new@test.com");
        assertThat(response.phoneNumber()).isEqualTo("987654321");
        assertThat(user.getEmail()).isEqualTo("juan.new@test.com");
        assertThat(user.getPhoneNumber()).isEqualTo("987654321");
    }

    @Test
    void updateUser_Fail_EmailTaken() {
        // Given
        Long userId = 1L;
        UserUpdateRequest request = new UserUpdateRequest(
                "taken@test.com",
                "987654321"
        );

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("taken@test.com"))
                .thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> userService.updateUser(request, userId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("El email ya está en uso por otro usuario.");
        assertThat(user.getEmail()).isEqualTo("juan@test.com");
    }

    // --- Tests: changePassword ---

    @Test
    void changePasswordSuccess() {
        // Given
        Long userId = 1L;
        ChangePasswordRequest request = new ChangePasswordRequest(
                "oldPassword123",
                "newPassword123",
                "newPassword123"
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("oldPassword123", "ENCODED_PASSWORD"))
                .thenReturn(true);
        when(passwordEncoder.matches("newPassword123", "ENCODED_PASSWORD"))
                .thenReturn(false);
        when(passwordEncoder.encode("newPassword123"))
                .thenReturn("NEW_ENCODED_HASH");

        // When
        userService.changePassword(request, userId);

        // Then
        assertThat(user.getPassword()).isEqualTo("NEW_ENCODED_HASH");
    }

    @Test
    void changePassword_Fail_WrongCurrentPassword() {
        // Given
        Long userId = 1L;
        ChangePasswordRequest request = new ChangePasswordRequest(
                "wrongOldPass",
                "new",
                "new"
        );

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongOldPass", "ENCODED_PASSWORD"))
                .thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> userService.changePassword(request, userId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("La contraseña actual es incorrecta");
    }

    @Test
    void changePassword_Fail_MismatchConfirmation() {
        // Given
        Long userId = 1L;
        ChangePasswordRequest request = new ChangePasswordRequest(
                "oldPassword123",
                "newPassA",
                "newPassB"
        );

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches("oldPassword123", "ENCODED_PASSWORD"))
                .thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> userService.changePassword(request, userId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("La nueva contraseña y la confirmación no coinciden.");
    }

    @Test
    void changePassword_Fail_NewSameAsOld() {
        // Given
        Long userId = 1L;
        ChangePasswordRequest request = new ChangePasswordRequest(
                "oldPassword123",
                "oldPassword123",
                "oldPassword123"
        );

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches("oldPassword123", "ENCODED_PASSWORD"))
                .thenReturn(true);
        when(passwordEncoder.matches("oldPassword123", "ENCODED_PASSWORD"))
                .thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> userService.changePassword(request, userId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("La nueva contraseña no puede ser igual a la actual.");
    }
}
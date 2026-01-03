package com.example.bank_app.service.impl;

import com.example.bank_app.model.LoginAttempt;
import com.example.bank_app.model.User;
import com.example.bank_app.repository.LoginAttemptRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginAttemptServiceImplTest {

    @Mock private LoginAttemptRepository loginAttemptRepository;
    @InjectMocks private LoginAttemptServiceImpl loginAttemptService;

    private User user;
    private LoginAttempt loginAttempt;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .email("test@example.com")
                .build();

        loginAttempt = LoginAttempt.builder()
                .id(100L)
                .user(user)
                .attempts(0)
                .isBlocked(false)
                .lastAttempt(null)
                .build();
    }

    // --- Create Login Attempt Tests ---

    @Test
    void createLoginAttempt_ShouldSaveNewRecord() {
        // When
        loginAttemptService.createLoginAttempt(user);

        // Then
        ArgumentCaptor<LoginAttempt> captor = ArgumentCaptor.forClass(LoginAttempt.class);
        verify(loginAttemptRepository).save(captor.capture());

        LoginAttempt saved = captor.getValue();
        assertThat(saved.getUser()).isEqualTo(user);
        assertThat(saved.getAttempts()).isEqualTo(0);
    }

    // --- Unblock User Tests ---

    @Test
    void unblockUser_Success() {
        // Given
        loginAttempt.setIsBlocked(true);
        loginAttempt.setAttempts(3);
        loginAttempt.setLastAttempt(LocalDateTime.now());

        when(loginAttemptRepository.findByUserIdAndIsBlockedTrue(user.getId()))
                .thenReturn(Optional.of(loginAttempt));

        // When
        loginAttemptService.unblockUser(user.getId());

        // Then
        assertThat(loginAttempt.getAttempts()).isEqualTo(0);
        assertThat(loginAttempt.getIsBlocked()).isFalse();
        assertThat(loginAttempt.getLastAttempt()).isNull();
    }

    @Test
    void unblockUser_Fail_UserNotBlockedOrNotFound() {
        // Given
        when(loginAttemptRepository.findByUserIdAndIsBlockedTrue(user.getId()))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> loginAttemptService.unblockUser(user.getId()))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Usuario no bloqueado o sin registro de intentos");
    }

    // --- Login Succeeded Tests ---

    @Test
    void loginSucceeded_ShouldResetAttempts() {
        // Given: Usuario con intentos fallidos previos pero no bloqueado
        loginAttempt.setAttempts(2);
        when(loginAttemptRepository.findByUserId(user.getId()))
                .thenReturn(Optional.of(loginAttempt));

        // When
        loginAttemptService.loginSucceeded(user);

        // Then
        assertThat(loginAttempt.getAttempts()).isEqualTo(0);
        assertThat(loginAttempt.getIsBlocked()).isFalse();
    }

    @Test
    void loginSucceeded_Fail_RelationNotCreated() {
        // Given
        when(loginAttemptRepository.findByUserId(user.getId()))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> loginAttemptService.loginSucceeded(user))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Relación no creada");
    }

    // --- Login Failed Tests ---

    @Test
    void loginFailed_NormalIncrement() {
        // Given: 0 intentos
        loginAttempt.setAttempts(0);
        when(loginAttemptRepository.findByUserId(user.getId()))
                .thenReturn(Optional.of(loginAttempt));

        // When
        loginAttemptService.loginFailed(user);

        // Then
        assertThat(loginAttempt.getAttempts()).isEqualTo(1);
        assertThat(loginAttempt.getIsBlocked()).isFalse();
    }

    @Test
    void loginFailed_ShouldBlockTemporarily_At3rdAttempt() {
        // Given: Ya tiene 2 intentos. El siguiente (3ro) debe bloquear.
        loginAttempt.setAttempts(2);
        when(loginAttemptRepository.findByUserId(user.getId()))
                .thenReturn(Optional.of(loginAttempt));

        // When
        loginAttemptService.loginFailed(user);

        // Then
        assertThat(loginAttempt.getAttempts()).isEqualTo(3);
        assertThat(loginAttempt.getIsBlocked()).isTrue();
    }

    @Test
    void loginFailed_ShouldBlockPermanently_At5thAttempt() {
        // Given: Ya tiene 4 intentos. El siguiente (5to) debe bloquear (permanente).
        loginAttempt.setAttempts(4);
        when(loginAttemptRepository.findByUserId(user.getId()))
                .thenReturn(Optional.of(loginAttempt));

        // When
        loginAttemptService.loginFailed(user);

        // Then
        assertThat(loginAttempt.getAttempts()).isEqualTo(5);
        assertThat(loginAttempt.getIsBlocked()).isTrue();
    }

    @Test
    void loginFailed_Fail_RelationNotCreated() {
        when(loginAttemptRepository.findByUserId(user.getId()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> loginAttemptService.loginFailed(user))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Relación no creada");
    }

    // --- Get Block Status Tests ---

    @Test
    void getBlockStatus_NotBlocked() {
        // Given
        loginAttempt.setIsBlocked(false);
        when(loginAttemptRepository.findByUserId(user.getId()))
                .thenReturn(Optional.of(loginAttempt));

        // When
        boolean isBlocked = loginAttemptService.getBlockStatus(user);

        // Then
        assertThat(isBlocked).isFalse();
    }

    @Test
    void getBlockStatus_PermanentBlock_ShouldRemainBlocked() {
        // Given: Bloqueado con 5 intentos (permanente) hace mucho tiempo
        loginAttempt.setIsBlocked(true);
        loginAttempt.setAttempts(5);
        loginAttempt.setLastAttempt(LocalDateTime.now().minusHours(1));

        when(loginAttemptRepository.findByUserId(user.getId()))
                .thenReturn(Optional.of(loginAttempt));

        // When
        boolean isBlocked = loginAttemptService.getBlockStatus(user);

        // Then
        assertThat(isBlocked).isTrue();
    }

    @Test
    void getBlockStatus_TempBlock_Active_ShouldRemainBlocked() {
        // Given: Bloqueado temporalmente hace 5 minutos
        loginAttempt.setIsBlocked(true);
        loginAttempt.setAttempts(3);
        loginAttempt.setLastAttempt(LocalDateTime.now().minusMinutes(5));

        when(loginAttemptRepository.findByUserId(user.getId()))
                .thenReturn(Optional.of(loginAttempt));

        // When
        boolean isBlocked = loginAttemptService.getBlockStatus(user);

        // Then
        assertThat(isBlocked).isTrue();
    }

    @Test
    void getBlockStatus_TempBlock_Expired_ShouldAutoUnblock() {
        // Given: Bloqueado temporalmente hace 15 minutos
        loginAttempt.setIsBlocked(true);
        loginAttempt.setAttempts(3);
        loginAttempt.setLastAttempt(LocalDateTime.now().minusMinutes(15));

        when(loginAttemptRepository.findByUserId(user.getId()))
                .thenReturn(Optional.of(loginAttempt));

        // When
        boolean isBlocked = loginAttemptService.getBlockStatus(user);

        // Then
        assertThat(isBlocked).isFalse();
        assertThat(loginAttempt.getIsBlocked()).isFalse();
    }

    @Test
    void getBlockStatus_Fail_RelationNotCreated() {
        when(loginAttemptRepository.findByUserId(user.getId()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> loginAttemptService.getBlockStatus(user))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Relación no creada");
    }
}
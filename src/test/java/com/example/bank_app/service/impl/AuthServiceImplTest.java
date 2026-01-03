package com.example.bank_app.service.impl;

import com.example.bank_app.config.JwtService;
import com.example.bank_app.dto.account.AccountCreationRequest;
import com.example.bank_app.dto.auth.AuthResponse;
import com.example.bank_app.dto.auth.LoginRequest;
import com.example.bank_app.dto.auth.RegisterRequest;
import com.example.bank_app.model.Role;
import com.example.bank_app.model.User;
import com.example.bank_app.repository.LoginAttemptRepository;
import com.example.bank_app.repository.RoleRepository;
import com.example.bank_app.repository.UserRepository;
import com.example.bank_app.service.AccountService;
import com.example.bank_app.service.LoginAttemptService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private AccountService accountService;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private LoginAttemptService loginAttemptService;

    @InjectMocks private AuthServiceImpl authService;

    private User user;
    private Role roleClient;

    @BeforeEach
    void setUp() {
        roleClient = Role.builder().id(1).name("CLIENT").build();

        user = User.builder()
                .id(1L)
                .email("test@mail.com")
                .password("ENCODED_PASS")
                .name("Test User")
                .documentId("12345678")
                .role(roleClient)
                .build();
    }

    // --- Login test ---

    @Test
    void loginSuccess() {
        // Given
        LoginRequest request = new LoginRequest(
                "test@mail.com",
                "password123"
        );

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(userRepository.findByEmail(request.email()))
                .thenReturn(Optional.of(user));
        when(jwtService.generateToken(user))
                .thenReturn("fake-jwt-token");

        // When
        AuthResponse response = authService.login(request);

        // Then
        assertThat(response.token()).isEqualTo("fake-jwt-token");
        assertThat(response.email()).isEqualTo("test@mail.com");
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void login_BadCredentials() {
        // Given
        LoginRequest request = new LoginRequest("test@mail.com", "wrongPass");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));
        when(userRepository.findByEmail(request.email()))
                .thenReturn(Optional.of(user));
        when(loginAttemptService.getBlockStatus(user))
                .thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class);
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void login_BlockedAccount() {
        // Given
        LoginRequest request = new LoginRequest(
                "test@mail.com",
                "password123"
        );

        when(userRepository.findByEmail(request.email()))
                .thenReturn(Optional.of(user));
        when(loginAttemptService.getBlockStatus(user))
                .thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(LockedException.class)
                .hasMessage("Tu cuenta ha sido bloqueada temporalmente");
        verify(jwtService, never()).generateToken(any());
    }

    // --- Tests: Register ---

    @Test
    void register_Success() {
        // Given
        RegisterRequest request = new RegisterRequest(
                "New Name",
                "Last1",
                "Last2",
                "87654321",
                "new@mail.com",
                "pass123",
                "999888777"
        );

        when(userRepository.existsByEmail(request.email()))
                .thenReturn(false);
        when(userRepository.existsByDocumentId(request.documentId()))
                .thenReturn(false);
        when(roleRepository.findByName("CLIENT"))
                .thenReturn(Optional.of(roleClient));
        when(passwordEncoder.encode(request.password()))
                .thenReturn("ENCODED_PASS");

        when(userRepository.save(any(User.class)))
                .thenAnswer(inv -> {
                    User u = inv.getArgument(0);
                    u.setId(200L);
                    return u;
                });
        when(jwtService.generateToken(any(User.class)))
                .thenReturn("new-jwt-token");

        // When
        AuthResponse response = authService.register(request);

        // Then
        assertThat(response.token()).isEqualTo("new-jwt-token");
        assertThat(response.role()).isEqualTo("CLIENT");
        verify(accountService).createAccount(any(AccountCreationRequest.class), any(User.class));
    }

    @Test
    void register_Fail_EmailExists() {
        // Given
        RegisterRequest request = new RegisterRequest(
                "Name",
                "L1",
                "L2",
                "111",
                "exiting@mail.com",
                "pass",
                "999"
        );

        when(userRepository.existsByEmail(request.email()))
                .thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("El email ya está registrado");
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_Fail_DocumentExists() {
        // Given
        RegisterRequest request = new RegisterRequest(
                "Name",
                "L1",
                "L2",
                "111",
                "new@mail.com",
                "pass",
                "999"
        );

        when(userRepository.existsByEmail(request.email()))
                .thenReturn(false);
        when(userRepository.existsByDocumentId(request.documentId()))
                .thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("El DNI ya está registrado");
    }

    @Test
    void register_Fail_RoleNotFound() {
        // Given
        RegisterRequest request = new RegisterRequest(
                "Name",
                "L1",
                "L2",
                "111",
                "new@mail.com",
                "pass",
                "999"
        );

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByDocumentId(anyString())).thenReturn(false);
        when(roleRepository.findByName("CLIENT")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Error: Rol por defecto no encontrado en BD");
    }
}
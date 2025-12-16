package com.example.bank_app.service.impl;

import com.example.bank_app.dto.user.ChangePasswordRequest;
import com.example.bank_app.dto.user.UserResponse;
import com.example.bank_app.dto.user.UserUpdateRequest;
import com.example.bank_app.model.User;
import com.example.bank_app.repository.UserRepository;
import com.example.bank_app.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserResponse updateUser(UserUpdateRequest request, Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario con ID: " + userId + ", no encontrado."));

        if (request.email() != null && !request.email().isBlank()) {
            if (!user.getEmail().equals(request.email())) {
                if (userRepository.existsByEmail(request.email())) {
                    throw new RuntimeException("El email ya está en uso por otro usuario.");
                }

                user.setEmail(request.email());
            }
        }

        if (request.phoneNumber() != null && !request.phoneNumber().isBlank()) {
            user.setPhoneNumber(request.phoneNumber());
        }

        return mapToUserResponse(user);
    }

    @Override
    public void changePassword(ChangePasswordRequest request, Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario con ID: " + userId + ", no encontrado."));

        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new RuntimeException("La contraseña actual es incorrecta");
        }

        if (!request.newPassword().equals(request.confirmationPassword())) {
            throw new RuntimeException("La nueva contraseña y la confirmación no coinciden.");
        }

        if (passwordEncoder.matches(request.newPassword(), user.getPassword())) {
            throw new RuntimeException("La nueva contraseña no puede ser igual a la actual.");
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserProfile(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario con ID: " + userId + ", no encontrado."));

        return mapToUserResponse(user);
    }

    private UserResponse mapToUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getLastName1(),
                user.getLastName2(),
                user.getDocumentId(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getRole().getName(),
                user.getCreatedAt()
        );
    }
}

package com.example.bank_app.service;

import com.example.bank_app.dto.user.ChangePasswordRequest;
import com.example.bank_app.dto.user.UserResponse;
import com.example.bank_app.dto.user.UserUpdateRequest;

public interface UserService {
    UserResponse updateUser(UserUpdateRequest request, Long userId);
    void changePassword(ChangePasswordRequest request, Long userId);
    UserResponse getUserProfile(Long userId);
}

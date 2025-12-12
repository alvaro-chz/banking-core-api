package com.example.bank_app.service;

import com.example.bank_app.dto.user.ChangePasswordRequest;
import com.example.bank_app.dto.user.UserResponse;
import com.example.bank_app.dto.user.UserUpdateRequest;

public interface UserService {
    UserResponse updateUser(UserUpdateRequest request, Integer userId);
    void changePassword(ChangePasswordRequest request, Integer userId);
    UserResponse getUserProfile(Integer userId);
}

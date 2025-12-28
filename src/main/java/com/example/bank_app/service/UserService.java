package com.example.bank_app.service;

import com.example.bank_app.dto.admin.UserAdminResponse;
import com.example.bank_app.dto.user.ChangePasswordRequest;
import com.example.bank_app.dto.user.UserResponse;
import com.example.bank_app.dto.user.UserUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    UserResponse updateUser(UserUpdateRequest request, Long userId);
    void changePassword(ChangePasswordRequest request, Long userId);
    UserResponse getUserProfile(Long userId);
    Page<UserAdminResponse> getUsers(
            String term,
            Boolean isActive,
            Boolean isBlocked,
            Pageable pageable
    );
}

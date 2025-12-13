package com.example.bank_app.controller;

import com.example.bank_app.dto.user.ChangePasswordRequest;
import com.example.bank_app.dto.user.UserResponse;
import com.example.bank_app.dto.user.UserUpdateRequest;
import com.example.bank_app.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> update(@RequestBody @Valid UserUpdateRequest request, @PathVariable Integer id) {
        return ResponseEntity.ok(userService.updateUser(request, id));
    }

    @PatchMapping("/{id}/password")
    public ResponseEntity<Void> changePassword(@RequestBody @Valid ChangePasswordRequest request, @PathVariable Integer id) {
        userService.changePassword(request, id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getProfile(@PathVariable Integer id) {
        return ResponseEntity.ok(userService.getUserProfile(id));
    }
}

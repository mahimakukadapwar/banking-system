package com.banking.controller;

import com.banking.dto.common.ApiResponse;
import com.banking.dto.user.*;
import com.banking.dto.common.BalanceResponse;

import com.banking.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserDTO>> getUserProfile(Authentication authentication) {

        String email = authentication.getName();
        UserDTO userDTO = userService.getUserProfile(email);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Profile retrieved successfully",
                        userDTO,
                        LocalDateTime.now()
                )
        );
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<UserDTO>> updateUserProfile(
            Authentication authentication,
            @Valid @RequestBody UpdateProfileRequest request) {

        String email = authentication.getName();
        UserDTO userDTO = userService.updateUserProfile(email, request);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Profile updated successfully",
                        userDTO,
                        LocalDateTime.now()
                )
        );
    }

    @GetMapping("/balance")
    public ResponseEntity<ApiResponse<BalanceResponse>> checkBalance(Authentication authentication) {

        String email = authentication.getName();
        BalanceResponse balanceResponse = userService.checkBalance(email);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Balance retrieved successfully",
                        balanceResponse,
                        LocalDateTime.now()
                )
        );
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<String>> changePassword(
            Authentication authentication,
            @Valid @RequestBody ChangePasswordRequest request) {

        String email = authentication.getName();
        userService.changePassword(email, request);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Password changed successfully",
                        "Your password has been updated",
                        LocalDateTime.now()
                )
        );
    }

    @PostMapping("/ban-account")
    public ResponseEntity<ApiResponse<String>> banAccount(
            Authentication authentication,
            @Valid @RequestBody BanAccountRequest request) {

        String email = authentication.getName();
        userService.banAccount(email, request.getReason());

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Account banned successfully",
                        "Your account has been banned",
                        LocalDateTime.now()
                )
        );
    }

    @PostMapping("/update-daily-limit")
    public ResponseEntity<ApiResponse<String>> updateDailyTransactionLimit(
            Authentication authentication,
            @Valid @RequestBody DailyTransactionLimitRequest request) {

        String email = authentication.getName();
        userService.updateDailyTransactionLimit(email, request);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Daily limit updated successfully",
                        "Your daily transaction limit has been updated to " + request.getDailyLimit(),
                        LocalDateTime.now()
                )
        );
    }
}
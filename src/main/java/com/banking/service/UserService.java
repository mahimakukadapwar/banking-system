package com.banking.service;

import com.banking.dto.common.BalanceResponse;
import com.banking.dto.user.ChangePasswordRequest;
import com.banking.dto.user.DailyTransactionLimitRequest;
import com.banking.dto.user.UpdateProfileRequest;
import com.banking.dto.user.UserDTO;
import com.banking.entity.User;
import com.banking.exception.*;
import com.banking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserDTO getUserProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        return convertToDTO(user);
    }

    public UserDTO updateUserProfile(String email, UpdateProfileRequest updateProfileRequest) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        user.setPhoneNumber(updateProfileRequest.getPhoneNumber());
        user.setAddress(updateProfileRequest.getAddress());
        user.setCity(updateProfileRequest.getCity());
        user.setState(updateProfileRequest.getState());
        user.setPinCode(updateProfileRequest.getPinCode());

        User updatedUser = userRepository.save(user);
        return convertToDTO(updatedUser);
    }

    public BalanceResponse checkBalance(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        return new BalanceResponse(
                user.getBalance(),
                user.getDailyTransactionLimit(),
                LocalDateTime.now()
        );
    }

    public void changePassword(String email, ChangePasswordRequest changePasswordRequest) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        // Verify old password
        if (!passwordEncoder.matches(changePasswordRequest.getOldPassword(), user.getPassword())) {
            throw new InvalidPasswordException("Old password is incorrect");
        }

        // Check if new password and confirm password match
        if (!changePasswordRequest.getNewPassword().equals(changePasswordRequest.getConfirmPassword())) {
            throw new InvalidPasswordException("New password and confirm password do not match");
        }

        // Check if old and new password are same
        if (changePasswordRequest.getOldPassword().equals(changePasswordRequest.getNewPassword())) {
            throw new InvalidPasswordException("New password cannot be the same as old password");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(changePasswordRequest.getNewPassword()));
        userRepository.save(user);
    }

    public void banAccount(String email, String reason) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        user.setAccountStatus(User.AccountStatus.BANNED);
        user.setIsActive(false);
        userRepository.save(user);
    }

    public void suspendAccount(String email, String reason) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        user.setAccountStatus(User.AccountStatus.SUSPENDED);
        user.setIsActive(false);
        userRepository.save(user);
    }

    public void activateAccount(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        user.setAccountStatus(User.AccountStatus.ACTIVE);
        user.setIsActive(true);
        userRepository.save(user);
    }

    public void updateDailyTransactionLimit(String email, DailyTransactionLimitRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        user.setDailyTransactionLimit(request.getDailyLimit());
        userRepository.save(user);
    }

    private UserDTO convertToDTO(User user) {
        return new UserDTO(
                user.getUserId(),
                user.getEmail(),
                user.getFullName(),
                user.getAccountNumber(),
                user.getBalance(),
                user.getPhoneNumber(),
                user.getAddress(),
                user.getCity(),
                user.getState(),
                user.getPinCode(),
                user.getAccountStatus().toString(),
                user.getIsActive(),
                user.getCreatedAt(),
                user.getLastLoginAt(),
                user.getDailyTransactionLimit()
        );
    }
}

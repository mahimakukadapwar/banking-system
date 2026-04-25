package com.banking.service;

import com.banking.dto.auth.RegisterRequest;
import com.banking.dto.auth.LoginRequest;
import com.banking.dto.auth.AuthResponse;
import com.banking.dto.user.UserDTO;
import com.banking.entity.User;
import com.banking.exception.*;
import com.banking.repository.UserRepository;
import com.banking.security.JwtTokenProvider;
import com.banking.util.AccountNumberGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthResponse register(RegisterRequest registerRequest) {

        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new DuplicateEmailException("Email already registered. Please use a different email.");
        }

        User user = User.builder()
                .email(registerRequest.getEmail())
                .fullName(registerRequest.getFullName())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .accountNumber(AccountNumberGenerator.generateAccountNumber())
                .balance(BigDecimal.ZERO)
                .phoneNumber(registerRequest.getPhoneNumber())
                .address(registerRequest.getAddress())
                .city(registerRequest.getCity())
                .state(registerRequest.getState())
                .pinCode(registerRequest.getPinCode())
                .accountStatus(User.AccountStatus.ACTIVE)
                .isActive(true)
                .dailyTransactionLimit(new BigDecimal("100000.00"))
                .build();

        User savedUser = userRepository.save(user);

        String token = jwtTokenProvider.generateToken(savedUser.getEmail());
        UserDTO userDTO = convertToDTO(savedUser);

        return new AuthResponse(token, userDTO);
    }

    public AuthResponse login(LoginRequest loginRequest) {

        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        if (user.getAccountStatus() == User.AccountStatus.BANNED) {
            throw new AccountBannedException("Your account has been banned. Please contact support.");
        }

        if (user.getAccountStatus() == User.AccountStatus.SUSPENDED) {
            throw new AccountSuspendedException("Your account has been suspended. Please contact support.");
        }

        if (!user.getIsActive()) {
            throw new BankingException("Your account is inactive. Please contact support.");
        }

        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        String token = jwtTokenProvider.generateToken(user.getEmail());
        UserDTO userDTO = convertToDTO(user);

        return new AuthResponse(token, userDTO);
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
package com.banking.dto.auth;

import com.banking.dto.user.UserDTO;
import lombok.*;



@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private String token;
    private UserDTO user;
}

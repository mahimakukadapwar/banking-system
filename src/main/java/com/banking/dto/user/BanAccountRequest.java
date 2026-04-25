package com.banking.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BanAccountRequest {

    @NotBlank(message = "Reason is required")
    private String reason;
}

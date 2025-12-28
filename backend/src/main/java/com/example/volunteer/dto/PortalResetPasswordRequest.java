package com.example.volunteer.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PortalResetPasswordRequest {
    @NotBlank
    private String phone;
    @NotBlank
    private String newPassword;
}

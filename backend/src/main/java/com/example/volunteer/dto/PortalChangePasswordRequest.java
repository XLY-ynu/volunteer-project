package com.example.volunteer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class PortalChangePasswordRequest {
    @NotBlank
    private String oldPassword;
    @NotBlank
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,20}$", message = "密码需8-20位且包含字母和数字")
    private String newPassword;
}

package com.example.volunteer.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PortalLoginRequest {
    @NotBlank
    private String phone;
    @NotBlank
    private String password;
}

package com.example.volunteer.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class HeartbeatRequest {
    @NotBlank
    private String code;
    @NotBlank
    private String status;
}

package com.example.volunteer.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ActivityCheckinPublicRequest {
    @NotBlank
    private String checkinCode;
    @NotBlank
    private String name;
    private String phone;
}

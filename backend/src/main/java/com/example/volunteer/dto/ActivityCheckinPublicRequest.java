package com.example.volunteer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ActivityCheckinPublicRequest {
    @NotNull
    private Long activityId;
    @NotBlank
    private String checkinCode;
    @NotBlank
    private String name;
    private String phone;
    private String email;
    private String organization;
}

package com.example.volunteer.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ActivitySignupRequest {
    @NotNull
    private Long activityId;
    @NotNull
    private Long volunteerId;
}

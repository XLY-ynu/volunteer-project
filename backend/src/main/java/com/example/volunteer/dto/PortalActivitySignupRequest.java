package com.example.volunteer.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PortalActivitySignupRequest {
    @NotNull
    private Long activityId;
}

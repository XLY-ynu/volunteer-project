package com.example.volunteer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ActivitySignupPublicRequest {
    @NotNull(message = "活动ID不能为空")
    private Long activityId;

    @NotBlank(message = "姓名不能为空")
    private String name;

    private String phone;
    private String email;
    private String organization;
}

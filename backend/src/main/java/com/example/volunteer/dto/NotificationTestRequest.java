package com.example.volunteer.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class NotificationTestRequest {
    @NotBlank
    private String channel;
    private String target;
    private String title;
    private String content;
}

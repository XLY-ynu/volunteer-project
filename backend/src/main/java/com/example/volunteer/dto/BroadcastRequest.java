package com.example.volunteer.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BroadcastRequest {
    @NotBlank
    private String title;
    private Long mediaId;
    private Long contentId;
    private String targetGroup;
    private String targetTerminalCode;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer priority;
    private String queueMode;
}

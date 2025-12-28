package com.example.volunteer.dto;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class PortalReminderDto {
    private Long activityId;
    private String title;
    private String location;
    private LocalDateTime startTime;
    private long countdownSeconds;
}

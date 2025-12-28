package com.example.volunteer.dto;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class PortalReminderLogDto {
    private Long id;
    private Long activityId;
    private String activityTitle;
    private String reminderType;
    private String channel;
    private String status;
    private String message;
    private LocalDateTime createdAt;
}

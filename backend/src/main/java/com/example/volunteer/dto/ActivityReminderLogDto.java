package com.example.volunteer.dto;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ActivityReminderLogDto {
    private Long id;
    private Long activityId;
    private String activityTitle;
    private Long volunteerId;
    private String volunteerName;
    private String volunteerPhone;
    private String reminderType;
    private String channel;
    private String status;
    private String message;
    private LocalDateTime createdAt;
}

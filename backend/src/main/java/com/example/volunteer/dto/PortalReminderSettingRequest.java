package com.example.volunteer.dto;

import lombok.Data;

@Data
public class PortalReminderSettingRequest {
    private Boolean signupReminder;
    private Boolean checkinReminder;
    private String channel;
    private Integer reminderMinutes;
}

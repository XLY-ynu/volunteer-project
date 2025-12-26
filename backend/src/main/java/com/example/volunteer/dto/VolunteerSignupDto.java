package com.example.volunteer.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class VolunteerSignupDto {
    private Long activityId;
    private String title;
    private String location;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
    private LocalDateTime signupTime;
    private LocalDateTime checkinTime;
}

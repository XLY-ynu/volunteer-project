package com.example.volunteer.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ActivityPublicDto {
    private Long id;
    private String title;
    private String description;
    private String location;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer capacity;
    private Integer signupCount; // 报名人数
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

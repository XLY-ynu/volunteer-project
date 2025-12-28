package com.example.volunteer.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PortalProfileDto {
    private Long id;
    private Long userId;
    private String name;
    private String phone;
    private String email;
    private String organization;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

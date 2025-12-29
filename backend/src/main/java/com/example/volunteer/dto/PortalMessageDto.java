package com.example.volunteer.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PortalMessageDto {
    private String key;
    private String title;
    private String message;
    private String type;
    private String status;
    private String channel;
    private LocalDateTime createdAt;
    private Boolean read;
}

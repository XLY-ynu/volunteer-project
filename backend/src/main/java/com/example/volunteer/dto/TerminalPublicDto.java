package com.example.volunteer.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TerminalPublicDto {
    private String code;
    private String name;
    private String groupName;
    private String status;
    private LocalDateTime lastHeartbeat;
}

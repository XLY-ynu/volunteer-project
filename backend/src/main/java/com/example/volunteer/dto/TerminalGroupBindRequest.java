package com.example.volunteer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TerminalGroupBindRequest {
    @NotBlank
    private String groupName;
    @NotNull
    private Long playlistId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}

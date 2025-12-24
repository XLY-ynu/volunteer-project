package com.example.volunteer.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class TerminalPlaylistBindRequest {
    @NotEmpty
    private List<Long> terminalIds;
    @NotNull
    private Long playlistId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}

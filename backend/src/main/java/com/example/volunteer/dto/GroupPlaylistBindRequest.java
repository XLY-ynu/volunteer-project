package com.example.volunteer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class GroupPlaylistBindRequest {
    @NotBlank(message = "分组名不能为空")
    private String groupName;

    @NotNull(message = "播放列表ID不能为空")
    private Long playlistId;

    private LocalDateTime startTime;

    private LocalDateTime endTime;
}

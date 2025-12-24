package com.example.volunteer.dto;

import lombok.Data;

@Data
public class PlaylistItemDto {
    private Long mediaId;
    private Long contentId;
    private Integer displayDuration;
    private Integer sortOrder;
}

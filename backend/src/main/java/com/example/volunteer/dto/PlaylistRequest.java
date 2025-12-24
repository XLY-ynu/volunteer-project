package com.example.volunteer.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class PlaylistRequest {
    @NotBlank
    private String name;
    private String description;
    private List<PlaylistItemDto> items;
}

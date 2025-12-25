package com.example.volunteer.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MediaAssetRequest {
    @NotBlank
    private String name;
    @NotBlank
    private String type;
    @NotBlank
    private String url;
    private String thumbUrl;
    private Long sizeBytes;
    private Integer durationSeconds;
    private Integer width;
    private Integer height;
    private String checksum;
}

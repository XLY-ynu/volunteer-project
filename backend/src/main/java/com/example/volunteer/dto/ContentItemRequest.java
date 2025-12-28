package com.example.volunteer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ContentItemRequest {
    @NotNull
    private Long categoryId;
    @NotBlank
    private String title;
    private String summary;
    private String body;
    private String coverUrl;
    private Boolean published;
    private Boolean headline;
    private Boolean recommended;
    private Integer recommendWeight;
    private Integer sortOrder;
}

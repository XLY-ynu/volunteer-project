package com.example.volunteer.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LayoutTemplateImportRequest {
    @NotBlank
    private String name;
    private String description;
    private String tags;
    @NotBlank
    private String layoutJson;
    private String coverUrl;
}

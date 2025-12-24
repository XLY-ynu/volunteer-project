package com.example.volunteer.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MenuCategoryRequest {
    @NotBlank
    private String name;
    @NotBlank
    private String code;
    private Long parentId;
    private Integer sortOrder;
}

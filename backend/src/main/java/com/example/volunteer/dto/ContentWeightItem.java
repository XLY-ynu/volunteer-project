package com.example.volunteer.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ContentWeightItem {
    @NotNull
    private Long id;
    @NotNull
    private Integer recommendWeight;
}

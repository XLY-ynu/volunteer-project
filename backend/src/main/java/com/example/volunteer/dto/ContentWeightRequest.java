package com.example.volunteer.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.Data;

@Data
public class ContentWeightRequest {
    @NotEmpty
    private List<ContentWeightItem> items;
}

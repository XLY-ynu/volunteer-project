package com.example.volunteer.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TerminalRequest {
    @NotBlank
    private String code;
    @NotBlank
    private String name;
    private String groupName;
    private String attributes;
}

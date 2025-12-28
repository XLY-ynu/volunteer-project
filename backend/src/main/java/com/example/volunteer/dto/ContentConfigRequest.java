package com.example.volunteer.dto;

import lombok.Data;

@Data
public class ContentConfigRequest {
    private Integer recommendIntervalSec;
    private Integer recommendCount;
    private String recommendStrategy;
    private Integer previewIntervalSec;
}

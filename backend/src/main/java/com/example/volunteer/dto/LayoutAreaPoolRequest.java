package com.example.volunteer.dto;

import lombok.Data;

import java.util.List;

@Data
public class LayoutAreaPoolRequest {
    private List<LayoutAreaPoolItem> items;

    @Data
    public static class LayoutAreaPoolItem {
        private Long mediaId;
        private Long contentId;
        private Integer displayDuration;
        private Integer sortOrder;
    }
}

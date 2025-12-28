package com.example.volunteer.dto;

import lombok.Data;

import java.util.List;

@Data
public class ContentOrderRequest {
    private List<ContentOrderItem> items;
}

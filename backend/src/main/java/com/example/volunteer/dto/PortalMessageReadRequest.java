package com.example.volunteer.dto;

import lombok.Data;

import java.util.List;

@Data
public class PortalMessageReadRequest {
    private List<String> keys;
    private Boolean readAll;
}

package com.example.volunteer.dto;

import java.util.Map;
import lombok.Data;

@Data
public class NotificationChannelConfigRequest {
    private Boolean enabled;
    private Map<String, Object> config;
}

package com.example.volunteer.controller;

import com.example.volunteer.common.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class HealthController {
    @GetMapping("/ping")
    public ApiResponse<String> ping() {
        return ApiResponse.ok("pong", "pong");
    }
}

/**
 * @Author: 陈力宏
 * @Module: 系统管理 - 认证控制器
 * @Description: 管理员登录/登出认证接口
 */
package com.example.volunteer.controller;

import com.example.volunteer.common.ApiResponse;
import com.example.volunteer.dto.LoginRequest;
import com.example.volunteer.dto.LoginResponse;
import com.example.volunteer.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok(authService.login(request));
    }
}

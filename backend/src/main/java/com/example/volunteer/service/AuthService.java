package com.example.volunteer.service;

import com.example.volunteer.dto.LoginRequest;
import com.example.volunteer.dto.LoginResponse;

public interface AuthService {
    LoginResponse login(LoginRequest request);
}

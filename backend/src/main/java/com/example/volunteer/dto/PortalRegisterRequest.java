package com.example.volunteer.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class PortalRegisterRequest {
    @NotBlank
    private String name;
    @NotBlank
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;
    @NotBlank
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,20}$", message = "密码需8-20位且包含字母和数字")
    private String password;
    @Email(message = "邮箱格式不正确")
    private String email;
    private String organization;
}

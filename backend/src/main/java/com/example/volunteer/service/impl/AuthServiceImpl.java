package com.example.volunteer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.volunteer.dto.LoginRequest;
import com.example.volunteer.dto.LoginResponse;
import com.example.volunteer.entity.User;
import com.example.volunteer.mapper.UserMapper;
import com.example.volunteer.security.JwtUtil;
import com.example.volunteer.service.AuthService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthServiceImpl(UserMapper userMapper, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, request.getUsername()));
        Assert.notNull(user, "用户不存在");
        Assert.isTrue(Boolean.TRUE.equals(user.getEnabled()), "账号已禁用");
        Assert.isTrue(passwordEncoder.matches(request.getPassword(), user.getPassword()), "密码错误");
        // 管理后台只允许 ADMIN 角色登录
        Assert.isTrue("ADMIN".equals(user.getRoleCode()), "您没有管理后台的访问权限");
        String token = jwtUtil.generateToken(user.getUsername(), user.getRoleCode());
        return new LoginResponse(token, user.getUsername(), user.getRoleCode());
    }
    
    /**
     * 志愿者组织端登录
     */
    public LoginResponse orgLogin(LoginRequest request) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, request.getUsername()));
        Assert.notNull(user, "用户不存在");
        Assert.isTrue(Boolean.TRUE.equals(user.getEnabled()), "账号已禁用");
        Assert.isTrue(passwordEncoder.matches(request.getPassword(), user.getPassword()), "密码错误");
        // 组织端只允许 ORG 角色登录
        Assert.isTrue("ORG".equals(user.getRoleCode()), "您没有组织管理端的访问权限");
        String token = jwtUtil.generateToken(user.getUsername(), user.getRoleCode());
        return new LoginResponse(token, user.getUsername(), user.getRoleCode());
    }
    
    /**
     * 通用登录（志愿者端/普通用户端）
     */
    public LoginResponse portalLogin(LoginRequest request) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, request.getUsername()));
        Assert.notNull(user, "用户不存在");
        Assert.isTrue(Boolean.TRUE.equals(user.getEnabled()), "账号已禁用");
        Assert.isTrue(passwordEncoder.matches(request.getPassword(), user.getPassword()), "密码错误");
        // 门户端允许 VOLUNTEER 和 USER 角色登录
        String role = user.getRoleCode();
        Assert.isTrue("VOLUNTEER".equals(role) || "USER".equals(role), "请使用志愿者或普通用户账号登录");
        String token = jwtUtil.generateToken(user.getUsername(), user.getRoleCode());
        return new LoginResponse(token, user.getUsername(), user.getRoleCode());
    }
}

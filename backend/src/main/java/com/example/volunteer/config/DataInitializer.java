package com.example.volunteer.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.volunteer.entity.User;
import com.example.volunteer.mapper.UserMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initAdmin(UserMapper userMapper, PasswordEncoder passwordEncoder) {
        return args -> {
            User existing = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, "admin"));
            if (existing == null) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setNickname("管理员");
                admin.setRoleCode("ADMIN");
                admin.setEnabled(true);
                admin.setCreatedAt(LocalDateTime.now());
                admin.setUpdatedAt(LocalDateTime.now());
                userMapper.insert(admin);
            }
        };
    }
}

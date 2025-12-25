package com.example.volunteer.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.volunteer.entity.MenuCategory;
import com.example.volunteer.entity.User;
import com.example.volunteer.mapper.MenuCategoryMapper;
import com.example.volunteer.mapper.UserMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initAdmin(UserMapper userMapper, MenuCategoryMapper menuCategoryMapper, PasswordEncoder passwordEncoder) {
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
            // seed six main categories if not present
            String[][] categories = {
                    {"文明XX", "wenming"},
                    {"XX志愿者APP", "app"},
                    {"XX志愿者网", "web"},
                    {"雷锋热线", "leifeng"},
                    {"公益活动", "gongyi"},
                    {"公益广告", "ad"}
            };
            for (String[] c : categories) {
                long count = menuCategoryMapper.selectCount(new LambdaQueryWrapper<MenuCategory>()
                        .eq(MenuCategory::getCode, c[1]));
                if (count == 0) {
                    MenuCategory mc = new MenuCategory();
                    mc.setName(c[0]);
                    mc.setCode(c[1]);
                    mc.setSortOrder(0);
                    menuCategoryMapper.insert(mc);
                }
            }
        };
    }
}

/**
 * @Author: 陈力宏
 * @Module: 系统管理 - 用户管理
 * @Description: 用户管理控制器，支持用户的增删改查、角色分配
 */
package com.example.volunteer.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.volunteer.common.ApiResponse;
import com.example.volunteer.entity.Role;
import com.example.volunteer.entity.User;
import com.example.volunteer.entity.Volunteer;
import com.example.volunteer.mapper.RoleMapper;
import com.example.volunteer.mapper.UserMapper;
import com.example.volunteer.mapper.VolunteerMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final PasswordEncoder passwordEncoder;
    private final VolunteerMapper volunteerMapper;

    public UserController(UserMapper userMapper, RoleMapper roleMapper, PasswordEncoder passwordEncoder, VolunteerMapper volunteerMapper) {
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
        this.passwordEncoder = passwordEncoder;
        this.volunteerMapper = volunteerMapper;
    }

    @GetMapping
    public ApiResponse<Page<User>> page(@RequestParam(defaultValue = "1") int page,
                                        @RequestParam(defaultValue = "10") int size,
                                        @RequestParam(required = false) String username) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (username != null && !username.isEmpty()) {
            wrapper.like(User::getUsername, username);
        }
        wrapper.orderByDesc(User::getCreatedAt);
        Page<User> result = userMapper.selectPage(new Page<>(page, size), wrapper);
        // 隐藏密码
        result.getRecords().forEach(u -> u.setPassword(null));
        return ApiResponse.ok(result);
    }

    @PostMapping
    public ApiResponse<User> create(@RequestBody User user) {
        // 检查用户名是否已存在
        User existing = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, user.getUsername()));
        if (existing != null) {
            return ApiResponse.fail("用户名已存在");
        }
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            return ApiResponse.fail("密码不能为空");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.insert(user);
        
        // 如果创建的是志愿者角色，自动创建对应的Volunteer记录
        if ("VOLUNTEER".equals(user.getRoleCode())) {
            // 检查是否已有Volunteer记录（通过手机号）
            Volunteer existingVolunteer = volunteerMapper.selectOne(new LambdaQueryWrapper<Volunteer>()
                    .eq(Volunteer::getPhone, user.getUsername()));
            
            if (existingVolunteer == null) {
                // 创建新的Volunteer记录
                Volunteer volunteer = new Volunteer();
                volunteer.setUserId(user.getId());
                volunteer.setPhone(user.getUsername());
                volunteer.setName(user.getNickname() != null ? user.getNickname() : user.getUsername());
                volunteer.setStatus("approved"); // 管理员创建的直接设为approved
                volunteer.setCreatedAt(LocalDateTime.now());
                volunteer.setUpdatedAt(LocalDateTime.now());
                volunteerMapper.insert(volunteer);
            } else if (existingVolunteer.getUserId() == null) {
                // 如果Volunteer记录存在但未关联用户，则关联
                existingVolunteer.setUserId(user.getId());
                existingVolunteer.setStatus("approved");
                existingVolunteer.setUpdatedAt(LocalDateTime.now());
                volunteerMapper.updateById(existingVolunteer);
            }
        }
        
        user.setPassword(null);
        return ApiResponse.ok(user);
    }

    @PutMapping("/{id}")
    public ApiResponse<User> update(@PathVariable Long id, @RequestBody User user) {
        User existing = userMapper.selectById(id);
        if (existing == null) {
            return ApiResponse.fail("用户不存在");
        }
        
        // 获取当前登录用户
        String currentUsername = getCurrentUsername();
        
        // 不能禁用自己
        if (existing.getUsername().equals(currentUsername) && !user.getEnabled()) {
            return ApiResponse.fail("不能禁用自己的账号");
        }
        
        // 检查用户名是否被其他用户使用
        if (user.getUsername() != null && !user.getUsername().equals(existing.getUsername())) {
            User other = userMapper.selectOne(new LambdaQueryWrapper<User>()
                    .eq(User::getUsername, user.getUsername())
                    .ne(User::getId, id));
            if (other != null) {
                return ApiResponse.fail("用户名已被使用");
            }
            existing.setUsername(user.getUsername());
        }
        
        existing.setNickname(user.getNickname());
        existing.setRoleCode(user.getRoleCode());
        existing.setEnabled(user.getEnabled());
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            existing.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        existing.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(existing);
        existing.setPassword(null);
        return ApiResponse.ok(existing);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            return ApiResponse.fail("用户不存在");
        }
        
        // 获取当前登录用户
        String currentUsername = getCurrentUsername();
        
        // 不能删除自己
        if (user.getUsername().equals(currentUsername)) {
            return ApiResponse.fail("不能删除自己的账号");
        }
        
        userMapper.deleteById(id);
        return ApiResponse.ok(null);
    }

    @GetMapping("/roles")
    public ApiResponse<List<Role>> roles() {
        return ApiResponse.ok(roleMapper.selectList(null));
    }
    
    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : null;
    }
}

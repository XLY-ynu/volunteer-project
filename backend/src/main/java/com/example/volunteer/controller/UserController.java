package com.example.volunteer.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.volunteer.common.ApiResponse;
import com.example.volunteer.entity.Role;
import com.example.volunteer.entity.User;
import com.example.volunteer.mapper.RoleMapper;
import com.example.volunteer.mapper.UserMapper;
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

    public UserController(UserMapper userMapper, RoleMapper roleMapper, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public ApiResponse<Page<User>> page(@RequestParam(defaultValue = "1") int page,
                                        @RequestParam(defaultValue = "10") int size) {
        Page<User> result = userMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<User>().orderByDesc(User::getCreatedAt));
        // 隐藏密码
        result.getRecords().forEach(u -> u.setPassword(null));
        return ApiResponse.ok(result);
    }

    @PostMapping
    public ApiResponse<User> create(@RequestBody User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.insert(user);
        user.setPassword(null);
        return ApiResponse.ok(user);
    }

    @PutMapping("/{id}")
    public ApiResponse<User> update(@PathVariable Long id, @RequestBody User user) {
        User existing = userMapper.selectById(id);
        if (existing == null) {
            return ApiResponse.fail("用户不存在");
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
        userMapper.deleteById(id);
        return ApiResponse.ok(null);
    }

    @GetMapping("/roles")
    public ApiResponse<List<Role>> roles() {
        return ApiResponse.ok(roleMapper.selectList(null));
    }
}

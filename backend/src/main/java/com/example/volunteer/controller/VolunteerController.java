package com.example.volunteer.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.volunteer.common.ApiResponse;
import com.example.volunteer.entity.User;
import com.example.volunteer.entity.Volunteer;
import com.example.volunteer.entity.VolunteerStatusLog;
import com.example.volunteer.mapper.UserMapper;
import com.example.volunteer.mapper.VolunteerMapper;
import com.example.volunteer.mapper.VolunteerStatusLogMapper;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/volunteers")
public class VolunteerController {

    private final VolunteerMapper volunteerMapper;
    private final UserMapper userMapper;
    private final VolunteerStatusLogMapper volunteerStatusLogMapper;

    public VolunteerController(VolunteerMapper volunteerMapper, UserMapper userMapper, VolunteerStatusLogMapper volunteerStatusLogMapper) {
        this.volunteerMapper = volunteerMapper;
        this.userMapper = userMapper;
        this.volunteerStatusLogMapper = volunteerStatusLogMapper;
    }

    @GetMapping
    public ApiResponse<Page<Volunteer>> list(@RequestParam(defaultValue = "1") int page,
                                             @RequestParam(defaultValue = "20") int size,
                                             @RequestParam(required = false) String name,
                                             @RequestParam(required = false) String status) {
        LambdaQueryWrapper<Volunteer> w = new LambdaQueryWrapper<>();
        if (name != null && !name.isEmpty()) w.like(Volunteer::getName, name);
        if (status != null && !status.isEmpty()) w.eq(Volunteer::getStatus, status);
        Page<Volunteer> p = new Page<>(page, size);
        volunteerMapper.selectPage(p, w);
        return ApiResponse.ok(p);
    }

    @PostMapping
    public ApiResponse<Volunteer> create(@Valid @RequestBody Volunteer volunteer) {
        // 检查手机号是否已存在
        if (volunteer.getPhone() != null && !volunteer.getPhone().isEmpty()) {
            Volunteer existing = volunteerMapper.selectOne(new LambdaQueryWrapper<Volunteer>()
                    .eq(Volunteer::getPhone, volunteer.getPhone()));
            if (existing != null) {
                return ApiResponse.fail("该手机号已注册");
            }
        }
        volunteer.setStatus(volunteer.getStatus() == null ? "pending" : volunteer.getStatus());
        volunteer.setCreatedAt(LocalDateTime.now());
        volunteer.setUpdatedAt(LocalDateTime.now());
        volunteerMapper.insert(volunteer);
        syncUserStatus(volunteer);
        logStatus(volunteer.getId(), volunteer.getStatus(), "创建志愿者");
        return ApiResponse.ok(volunteer);
    }

    @PutMapping("/{id}")
    public ApiResponse<Volunteer> update(@PathVariable Long id, @Valid @RequestBody Volunteer volunteer) {
        // 检查手机号是否被其他人使用
        if (volunteer.getPhone() != null && !volunteer.getPhone().isEmpty()) {
            Volunteer existing = volunteerMapper.selectOne(new LambdaQueryWrapper<Volunteer>()
                    .eq(Volunteer::getPhone, volunteer.getPhone())
                    .ne(Volunteer::getId, id));
            if (existing != null) {
                return ApiResponse.fail("该手机号已被其他志愿者使用");
            }
        }
        // 获取原有记录以保留userId
        Volunteer existingVol = volunteerMapper.selectById(id);
        String previousStatus = existingVol != null ? existingVol.getStatus() : null;
        if (existingVol != null && volunteer.getUserId() == null) {
            volunteer.setUserId(existingVol.getUserId());
        }
        volunteer.setId(id);
        volunteer.setUpdatedAt(LocalDateTime.now());
        volunteerMapper.updateById(volunteer);
        syncUserStatus(volunteer);
        if (previousStatus != null && volunteer.getStatus() != null && !previousStatus.equals(volunteer.getStatus())) {
            logStatus(id, volunteer.getStatus(), "管理员更新状态");
        }
        return ApiResponse.ok(volunteer);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        volunteerMapper.deleteById(id);
        return ApiResponse.ok(null);
    }

    private void syncUserStatus(Volunteer volunteer) {
        if (volunteer.getUserId() == null) {
            return;
        }
        User user = userMapper.selectById(volunteer.getUserId());
        if (user == null) {
            return;
        }
        if ("approved".equals(volunteer.getStatus())) {
            user.setEnabled(true);
        } else {
            user.setEnabled(false);
        }
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
    }

    private void logStatus(Long volunteerId, String status, String remark) {
        VolunteerStatusLog log = new VolunteerStatusLog();
        log.setVolunteerId(volunteerId);
        log.setStatus(status);
        log.setRemark(remark);
        log.setCreatedAt(LocalDateTime.now());
        volunteerStatusLogMapper.insert(log);
    }
}

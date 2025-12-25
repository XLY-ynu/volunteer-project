package com.example.volunteer.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.volunteer.common.ApiResponse;
import com.example.volunteer.entity.ActivityCheckinLog;
import com.example.volunteer.entity.ActivitySignup;
import com.example.volunteer.mapper.ActivityCheckinLogMapper;
import com.example.volunteer.mapper.ActivitySignupMapper;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/activities")
public class ActivityCheckinController {

    private final ActivitySignupMapper activitySignupMapper;
    private final ActivityCheckinLogMapper activityCheckinLogMapper;

    public ActivityCheckinController(ActivitySignupMapper activitySignupMapper, ActivityCheckinLogMapper activityCheckinLogMapper) {
        this.activitySignupMapper = activitySignupMapper;
        this.activityCheckinLogMapper = activityCheckinLogMapper;
    }

    @PostMapping("/{id}/checkin")
    public ApiResponse<ActivitySignup> checkin(@PathVariable Long id, @RequestParam Long volunteerId) {
        ActivitySignup signup = activitySignupMapper.selectOne(new LambdaQueryWrapper<ActivitySignup>()
                .eq(ActivitySignup::getActivityId, id)
                .eq(ActivitySignup::getVolunteerId, volunteerId));
        if (signup == null) {
            return ApiResponse.fail("未报名");
        }
        signup.setStatus("checked_in");
        signup.setCheckinTime(LocalDateTime.now());
        activitySignupMapper.updateById(signup);

        ActivityCheckinLog log = new ActivityCheckinLog();
        log.setActivityId(id);
        log.setVolunteerId(volunteerId);
        log.setCreatedAt(LocalDateTime.now());
        activityCheckinLogMapper.insert(log);
        return ApiResponse.ok(signup);
    }

    @GetMapping("/{id}/checkin-logs")
    public ApiResponse<Page<ActivityCheckinLog>> logs(@PathVariable Long id,
                                                      @RequestParam(defaultValue = "1") int page,
                                                      @RequestParam(defaultValue = "50") int size) {
        Page<ActivityCheckinLog> p = new Page<>(page, size);
        activityCheckinLogMapper.selectPage(p, new LambdaQueryWrapper<ActivityCheckinLog>()
                .eq(ActivityCheckinLog::getActivityId, id)
                .orderByDesc(ActivityCheckinLog::getCreatedAt));
        return ApiResponse.ok(p);
    }
}

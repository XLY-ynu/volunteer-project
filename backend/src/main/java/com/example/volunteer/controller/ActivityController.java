package com.example.volunteer.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.volunteer.common.ApiResponse;
import com.example.volunteer.dto.ActivityRequest;
import com.example.volunteer.dto.ActivitySignupRequest;
import com.example.volunteer.entity.Activity;
import com.example.volunteer.entity.ActivitySignup;
import com.example.volunteer.entity.Volunteer;
import com.example.volunteer.mapper.ActivityMapper;
import com.example.volunteer.mapper.ActivitySignupMapper;
import com.example.volunteer.mapper.VolunteerMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/activities")
public class ActivityController {

    private final ActivityMapper activityMapper;
    private final ActivitySignupMapper activitySignupMapper;
    private final VolunteerMapper volunteerMapper;

    public ActivityController(ActivityMapper activityMapper, ActivitySignupMapper activitySignupMapper, VolunteerMapper volunteerMapper) {
        this.activityMapper = activityMapper;
        this.activitySignupMapper = activitySignupMapper;
        this.volunteerMapper = volunteerMapper;
    }

    @GetMapping
    public ApiResponse<Page<Activity>> list(@RequestParam(defaultValue = "1") int page,
                                            @RequestParam(defaultValue = "20") int size,
                                            @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<Activity> w = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            w.like(Activity::getTitle, keyword).or().like(Activity::getDescription, keyword);
        }
        Page<Activity> p = new Page<>(page, size);
        activityMapper.selectPage(p, w);
        return ApiResponse.ok(p);
    }

    @PostMapping
    public ApiResponse<Activity> create(@Valid @RequestBody ActivityRequest request) {
        Activity a = new Activity();
        a.setTitle(request.getTitle());
        a.setDescription(request.getDescription());
        a.setLocation(request.getLocation());
        a.setStartTime(request.getStartTime());
        a.setEndTime(request.getEndTime());
        a.setCapacity(request.getCapacity());
        a.setCheckinCode(request.getCheckinCode() != null ? request.getCheckinCode() : generateCode());
        a.setCreatedAt(LocalDateTime.now());
        a.setUpdatedAt(LocalDateTime.now());
        activityMapper.insert(a);
        return ApiResponse.ok(a);
    }

    @PutMapping("/{id}")
    public ApiResponse<Activity> update(@PathVariable Long id, @Valid @RequestBody ActivityRequest request) {
        Activity a = activityMapper.selectById(id);
        if (a == null) return ApiResponse.fail("不存在");
        a.setTitle(request.getTitle());
        a.setDescription(request.getDescription());
        a.setLocation(request.getLocation());
        a.setStartTime(request.getStartTime());
        a.setEndTime(request.getEndTime());
        a.setCapacity(request.getCapacity());
        a.setCheckinCode(request.getCheckinCode() != null ? request.getCheckinCode() : a.getCheckinCode());
        a.setUpdatedAt(LocalDateTime.now());
        activityMapper.updateById(a);
        return ApiResponse.ok(a);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        activityMapper.deleteById(id);
        return ApiResponse.ok(null);
    }

    @GetMapping("/{id}/signups")
    public ApiResponse<Page<ActivitySignup>> signups(@PathVariable Long id,
                                                     @RequestParam(defaultValue = "1") int page,
                                                     @RequestParam(defaultValue = "50") int size) {
        Page<ActivitySignup> p = new Page<>(page, size);
        activitySignupMapper.selectPage(p, new LambdaQueryWrapper<ActivitySignup>().eq(ActivitySignup::getActivityId, id));
        return ApiResponse.ok(p);
    }

    @PostMapping("/signup")
    public ApiResponse<ActivitySignup> signup(@Valid @RequestBody ActivitySignupRequest request) {
        ActivitySignup signup = new ActivitySignup();
        signup.setActivityId(request.getActivityId());
        signup.setVolunteerId(request.getVolunteerId());
        signup.setStatus("applied");
        signup.setCreatedAt(LocalDateTime.now());
        activitySignupMapper.insert(signup);
        return ApiResponse.ok(signup);
    }

    @PostMapping("/signup/checkin/{id}")
    public ApiResponse<ActivitySignup> checkin(@PathVariable Long id) {
        ActivitySignup signup = activitySignupMapper.selectById(id);
        if (signup == null) return ApiResponse.fail("报名不存在");
        signup.setStatus("checked_in");
        signup.setCheckinTime(LocalDateTime.now());
        activitySignupMapper.updateById(signup);
        return ApiResponse.ok(signup);
    }

    @GetMapping("/{id}/stats")
    public ApiResponse<?> stats(@PathVariable Long id) {
        long total = activitySignupMapper.selectCount(new LambdaQueryWrapper<ActivitySignup>().eq(ActivitySignup::getActivityId, id));
        long checked = activitySignupMapper.selectCount(new LambdaQueryWrapper<ActivitySignup>()
                .eq(ActivitySignup::getActivityId, id).eq(ActivitySignup::getStatus, "checked_in"));
        java.util.Map<String, Object> map = new java.util.HashMap<>();
        map.put("total", total);
        map.put("checkedIn", checked);
        Activity a = activityMapper.selectById(id);
        map.put("checkinCode", a != null ? a.getCheckinCode() : null);
        return ApiResponse.ok(map);
    }

    @GetMapping("/{id}/signups/export")
    public ResponseEntity<byte[]> exportSignups(@PathVariable Long id) {
        List<ActivitySignup> records = activitySignupMapper.selectList(new LambdaQueryWrapper<ActivitySignup>()
                .eq(ActivitySignup::getActivityId, id));
        StringBuilder sb = new StringBuilder();
        sb.append("volunteerId,status,createdAt,checkinTime\n");
        for (ActivitySignup s : records) {
            sb.append(s.getVolunteerId()).append(",")
                    .append(s.getStatus()).append(",")
                    .append(s.getCreatedAt() != null ? s.getCreatedAt() : "").append(",")
                    .append(s.getCheckinTime() != null ? s.getCheckinTime() : "").append("\n");
        }
        byte[] bytes = sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=activity-" + id + "-signups.csv")
                .contentType(MediaType.TEXT_PLAIN)
                .contentLength(bytes.length)
                .body(bytes);
    }

    private String generateCode() {
        return String.valueOf(100000 + new java.util.Random().nextInt(900000));
    }
}

package com.example.volunteer.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.volunteer.common.ApiResponse;
import com.example.volunteer.dto.ActivityRequest;
import com.example.volunteer.dto.ActivitySignupRequest;
import com.example.volunteer.entity.Activity;
import com.example.volunteer.entity.ActivityReminderLog;
import com.example.volunteer.entity.ActivitySignup;
import com.example.volunteer.entity.Volunteer;
import com.example.volunteer.entity.VolunteerMessage;
import com.example.volunteer.mapper.ActivityMapper;
import com.example.volunteer.mapper.ActivityReminderLogMapper;
import com.example.volunteer.mapper.ActivitySignupMapper;
import com.example.volunteer.mapper.VolunteerMapper;
import com.example.volunteer.mapper.VolunteerMessageMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/activities")
public class ActivityController {

    private final ActivityMapper activityMapper;
    private final ActivitySignupMapper activitySignupMapper;
    private final VolunteerMapper volunteerMapper;
    private final VolunteerMessageMapper volunteerMessageMapper;
    private final ActivityReminderLogMapper activityReminderLogMapper;

    public ActivityController(ActivityMapper activityMapper, 
                              ActivitySignupMapper activitySignupMapper, 
                              VolunteerMapper volunteerMapper,
                              VolunteerMessageMapper volunteerMessageMapper,
                              ActivityReminderLogMapper activityReminderLogMapper) {
        this.activityMapper = activityMapper;
        this.activitySignupMapper = activitySignupMapper;
        this.volunteerMapper = volunteerMapper;
        this.volunteerMessageMapper = volunteerMessageMapper;
        this.activityReminderLogMapper = activityReminderLogMapper;
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
        // 如果签到码为空或空字符串，自动生成
        String code = request.getCheckinCode();
        a.setCheckinCode((code != null && !code.trim().isEmpty()) ? code : generateCode());
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
        // 如果签到码为空或空字符串，保留原有签到码
        String code = request.getCheckinCode();
        if (code != null && !code.trim().isEmpty()) {
            a.setCheckinCode(code);
        } else if (a.getCheckinCode() == null || a.getCheckinCode().trim().isEmpty()) {
            // 如果原来也没有签到码，生成一个
            a.setCheckinCode(generateCode());
        }
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
    public ApiResponse<?> signups(@PathVariable Long id,
                                  @RequestParam(defaultValue = "1") int page,
                                  @RequestParam(defaultValue = "50") int size) {
        Page<ActivitySignup> p = new Page<>(page, size);
        activitySignupMapper.selectPage(p, new LambdaQueryWrapper<ActivitySignup>().eq(ActivitySignup::getActivityId, id));
        
        // 获取志愿者详细信息
        List<java.util.Map<String, Object>> result = p.getRecords().stream().map(signup -> {
            java.util.Map<String, Object> map = new java.util.HashMap<>();
            map.put("id", signup.getId());
            map.put("activityId", signup.getActivityId());
            map.put("volunteerId", signup.getVolunteerId());
            map.put("status", signup.getStatus());
            map.put("createdAt", signup.getCreatedAt());
            map.put("checkinTime", signup.getCheckinTime());
            
            // 获取志愿者信息
            Volunteer volunteer = volunteerMapper.selectById(signup.getVolunteerId());
            if (volunteer != null) {
                map.put("volunteerName", volunteer.getName());
                map.put("volunteerPhone", volunteer.getPhone());
                map.put("volunteerEmail", volunteer.getEmail());
                map.put("volunteerOrganization", volunteer.getOrganization());
            }
            return map;
        }).collect(Collectors.toList());
        
        java.util.Map<String, Object> pageResult = new java.util.HashMap<>();
        pageResult.put("records", result);
        pageResult.put("total", p.getTotal());
        pageResult.put("current", p.getCurrent());
        pageResult.put("size", p.getSize());
        
        return ApiResponse.ok(pageResult);
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
        sb.append("姓名,电话,邮箱,组织,状态,报名时间,签到时间\n");
        for (ActivitySignup s : records) {
            Volunteer v = volunteerMapper.selectById(s.getVolunteerId());
            String name = v != null ? v.getName() : "";
            String phone = v != null ? v.getPhone() : "";
            String email = v != null ? v.getEmail() : "";
            String org = v != null ? v.getOrganization() : "";
            String status = "checked_in".equals(s.getStatus()) ? "已签到" : "已报名";
            sb.append(name).append(",")
                    .append(phone).append(",")
                    .append(email).append(",")
                    .append(org).append(",")
                    .append(status).append(",")
                    .append(s.getCreatedAt() != null ? s.getCreatedAt().toString().replace("T", " ") : "").append(",")
                    .append(s.getCheckinTime() != null ? s.getCheckinTime().toString().replace("T", " ") : "").append("\n");
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

    /**
     * 向活动的报名志愿者发送站内提醒消息
     * 支持：全部发送、指定志愿者发送、自定义消息内容、提醒类型
     */
    @PostMapping("/{id}/send-reminder")
    public ApiResponse<?> sendReminder(@PathVariable Long id, @RequestBody(required = false) java.util.Map<String, Object> body) {
        Activity activity = activityMapper.selectById(id);
        if (activity == null) {
            return ApiResponse.fail("活动不存在");
        }
        
        // 解析参数
        String customContent = body != null ? (String) body.get("content") : null;
        String reminderType = body != null ? (String) body.get("type") : "checkin"; // checkin, signup, custom
        @SuppressWarnings("unchecked")
        List<Long> volunteerIds = body != null ? (List<Long>) body.get("volunteerIds") : null;
        String targetStatus = body != null ? (String) body.get("targetStatus") : null; // applied, checked_in, all
        
        // 获取目标志愿者
        LambdaQueryWrapper<ActivitySignup> query = new LambdaQueryWrapper<ActivitySignup>()
                .eq(ActivitySignup::getActivityId, id);
        
        // 根据状态筛选
        if ("applied".equals(targetStatus)) {
            query.eq(ActivitySignup::getStatus, "applied");
        } else if ("checked_in".equals(targetStatus)) {
            query.eq(ActivitySignup::getStatus, "checked_in");
        }
        // targetStatus 为 null 或 "all" 时不筛选
        
        List<ActivitySignup> signups = activitySignupMapper.selectList(query);
        
        // 如果指定了志愿者ID列表，则只发送给这些志愿者
        if (volunteerIds != null && !volunteerIds.isEmpty()) {
            signups = signups.stream()
                    .filter(s -> volunteerIds.contains(s.getVolunteerId()))
                    .collect(Collectors.toList());
        }
        
        if (signups.isEmpty()) {
            return ApiResponse.fail("没有符合条件的志愿者");
        }
        
        // 生成消息内容
        String title;
        String content;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String startTimeStr = activity.getStartTime() != null ? activity.getStartTime().format(formatter) : "待定";
        String location = activity.getLocation() != null ? activity.getLocation() : "待定";
        
        if (customContent != null && !customContent.trim().isEmpty()) {
            title = "活动通知 · " + activity.getTitle();
            content = customContent;
        } else if ("signup".equals(reminderType)) {
            title = "报名确认 · " + activity.getTitle();
            content = String.format("您已成功报名活动【%s】，活动时间：%s，地点：%s，请准时参加！", 
                    activity.getTitle(), startTimeStr, location);
        } else {
            // 默认签到提醒
            title = "签到提醒 · " + activity.getTitle();
            content = String.format("您报名的活动【%s】即将开始，时间：%s，地点：%s，请准时到场签到！签到码：%s", 
                    activity.getTitle(), startTimeStr, location, 
                    activity.getCheckinCode() != null ? activity.getCheckinCode() : "现场获取");
        }
        
        int sentCount = 0;
        for (ActivitySignup signup : signups) {
            // 发送站内消息
            VolunteerMessage message = new VolunteerMessage();
            message.setVolunteerId(signup.getVolunteerId());
            message.setActivityId(id);
            message.setTitle(title);
            message.setContent(content);
            message.setType("reminder");
            message.setIsRead(false);
            message.setCreatedAt(LocalDateTime.now());
            volunteerMessageMapper.insert(message);
            
            // 记录提醒日志
            ActivityReminderLog log = new ActivityReminderLog();
            log.setActivityId(id);
            log.setVolunteerId(signup.getVolunteerId());
            log.setReminderType(reminderType != null ? reminderType : "checkin");
            log.setChannel("站内消息");
            log.setStatus("sent");
            log.setMessage(content);
            log.setCreatedAt(LocalDateTime.now());
            activityReminderLogMapper.insert(log);
            
            sentCount++;
        }
        
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("sentCount", sentCount);
        result.put("message", "已成功发送 " + sentCount + " 条站内消息");
        return ApiResponse.ok(result);
    }
}

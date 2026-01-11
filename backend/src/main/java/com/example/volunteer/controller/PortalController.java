/**
 * @Author: 贺嘉伟
 * @Module: 活动参与模块
 * @Description: 志愿者端控制器，支持登录/注册、个人信息、参与记录、活动报名与签到
 */
package com.example.volunteer.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.volunteer.common.ApiResponse;
import com.example.volunteer.dto.LoginResponse;
import com.example.volunteer.dto.PortalActivitySignupRequest;
import com.example.volunteer.dto.PortalChangePasswordRequest;
import com.example.volunteer.dto.PortalLoginRequest;
import com.example.volunteer.dto.PortalMessageDto;
import com.example.volunteer.dto.PortalMessageReadRequest;
import com.example.volunteer.dto.PortalProfileDto;
import com.example.volunteer.dto.PortalProfileRequest;
import com.example.volunteer.dto.PortalReminderDto;
import com.example.volunteer.dto.PortalReminderLogDto;
import com.example.volunteer.dto.PortalReminderSettingRequest;
import com.example.volunteer.dto.PortalRegisterRequest;
import com.example.volunteer.dto.PortalResetPasswordRequest;
import com.example.volunteer.dto.VolunteerSignupDto;
import com.example.volunteer.entity.PortalMessageRead;
import com.example.volunteer.entity.Activity;
import com.example.volunteer.entity.ActivitySignup;
import com.example.volunteer.entity.ActivityReminderLog;
import com.example.volunteer.entity.User;
import com.example.volunteer.entity.Volunteer;
import com.example.volunteer.entity.VolunteerMessage;
import com.example.volunteer.entity.VolunteerReminderSetting;
import com.example.volunteer.entity.VolunteerStatusLog;
import com.example.volunteer.mapper.ActivityMapper;
import com.example.volunteer.mapper.ActivityReminderLogMapper;
import com.example.volunteer.mapper.ActivitySignupMapper;
import com.example.volunteer.mapper.PortalMessageReadMapper;
import com.example.volunteer.mapper.UserMapper;
import com.example.volunteer.mapper.VolunteerMapper;
import com.example.volunteer.mapper.VolunteerMessageMapper;
import com.example.volunteer.mapper.VolunteerOrgMapper;
import com.example.volunteer.mapper.VolunteerOrgMemberMapper;
import com.example.volunteer.mapper.VolunteerReminderSettingMapper;
import com.example.volunteer.mapper.VolunteerStatusLogMapper;
import com.example.volunteer.entity.VolunteerOrg;
import com.example.volunteer.entity.VolunteerOrgMember;
import com.example.volunteer.security.JwtUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/portal")
public class PortalController {

    private static final String PHONE_REGEX = "^1[3-9]\\d{9}$";
    private final UserMapper userMapper;
    private final VolunteerMapper volunteerMapper;
    private final ActivityMapper activityMapper;
    private final ActivitySignupMapper activitySignupMapper;
    private final VolunteerStatusLogMapper volunteerStatusLogMapper;
    private final VolunteerReminderSettingMapper reminderSettingMapper;
    private final ActivityReminderLogMapper activityReminderLogMapper;
    private final PortalMessageReadMapper portalMessageReadMapper;
    private final VolunteerMessageMapper volunteerMessageMapper;
    private final VolunteerOrgMapper volunteerOrgMapper;
    private final VolunteerOrgMemberMapper volunteerOrgMemberMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public PortalController(UserMapper userMapper,
                            VolunteerMapper volunteerMapper,
                            ActivityMapper activityMapper,
                            ActivitySignupMapper activitySignupMapper,
                            VolunteerStatusLogMapper volunteerStatusLogMapper,
                            VolunteerReminderSettingMapper reminderSettingMapper,
                            ActivityReminderLogMapper activityReminderLogMapper,
                            PortalMessageReadMapper portalMessageReadMapper,
                            VolunteerMessageMapper volunteerMessageMapper,
                            VolunteerOrgMapper volunteerOrgMapper,
                            VolunteerOrgMemberMapper volunteerOrgMemberMapper,
                            PasswordEncoder passwordEncoder,
                            JwtUtil jwtUtil) {
        this.userMapper = userMapper;
        this.volunteerMapper = volunteerMapper;
        this.activityMapper = activityMapper;
        this.activitySignupMapper = activitySignupMapper;
        this.volunteerStatusLogMapper = volunteerStatusLogMapper;
        this.reminderSettingMapper = reminderSettingMapper;
        this.activityReminderLogMapper = activityReminderLogMapper;
        this.portalMessageReadMapper = portalMessageReadMapper;
        this.volunteerMessageMapper = volunteerMessageMapper;
        this.volunteerOrgMapper = volunteerOrgMapper;
        this.volunteerOrgMemberMapper = volunteerOrgMemberMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/auth/register")
    public ApiResponse<LoginResponse> register(@Valid @RequestBody PortalRegisterRequest request) {
        User existingUser = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, request.getPhone()));
        Assert.isNull(existingUser, "手机号已注册，请直接登录");

        Volunteer existingVolunteer = volunteerMapper.selectOne(new LambdaQueryWrapper<Volunteer>()
                .eq(Volunteer::getPhone, request.getPhone()));
        if (existingVolunteer != null && "rejected".equals(existingVolunteer.getStatus())) {
            return ApiResponse.fail("该手机号已被拒绝，请联系管理员");
        }
        if (existingVolunteer != null && existingVolunteer.getUserId() != null) {
            return ApiResponse.fail("该手机号已绑定账户，请直接登录");
        }

        User user = new User();
        user.setUsername(request.getPhone());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getName());
        user.setRoleCode("VOLUNTEER");
        boolean approved = existingVolunteer != null && "approved".equals(existingVolunteer.getStatus());
        user.setEnabled(approved);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.insert(user);

        Volunteer volunteer = existingVolunteer;
        if (volunteer == null) {
            volunteer = new Volunteer();
            volunteer.setName(request.getName());
            volunteer.setPhone(request.getPhone());
            volunteer.setEmail(request.getEmail());
            volunteer.setOrganization(request.getOrganization());
            volunteer.setStatus(approved ? "approved" : "pending");
            volunteer.setUserId(user.getId());
            volunteer.setCreatedAt(LocalDateTime.now());
            volunteer.setUpdatedAt(LocalDateTime.now());
            volunteerMapper.insert(volunteer);
            logStatus(volunteer.getId(), volunteer.getStatus(), "门户注册");
        } else {
            volunteer.setUserId(user.getId());
            if (volunteer.getStatus() == null) {
                volunteer.setStatus(approved ? "approved" : "pending");
            }
            if (request.getName() != null && !request.getName().isEmpty()) {
                volunteer.setName(request.getName());
            }
            if (request.getEmail() != null) {
                volunteer.setEmail(request.getEmail());
            }
            if (request.getOrganization() != null) {
                volunteer.setOrganization(request.getOrganization());
            }
            volunteer.setUpdatedAt(LocalDateTime.now());
            volunteerMapper.updateById(volunteer);
            logStatus(volunteer.getId(), volunteer.getStatus(), "门户绑定账号");
        }

        String token = approved ? jwtUtil.generateToken(user.getUsername(), user.getRoleCode()) : "";
        return ApiResponse.ok(new LoginResponse(token, user.getUsername(), user.getRoleCode()));
    }

    @PostMapping("/auth/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody PortalLoginRequest request) {
        User user = null;
        Volunteer volunteer = null;
        String loginId = request.getPhone(); // 可以是手机号或用户名
        
        // 方式1: 先尝试用输入作为用户名查找
        user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, loginId));
        
        // 方式2: 如果没找到，尝试通过手机号查找志愿者记录，再找关联的用户
        if (user == null) {
            volunteer = volunteerMapper.selectOne(new LambdaQueryWrapper<Volunteer>()
                    .eq(Volunteer::getPhone, loginId));
            if (volunteer != null && volunteer.getUserId() != null) {
                user = userMapper.selectById(volunteer.getUserId());
            }
        }
        
        if (user == null) {
            return ApiResponse.fail("用户不存在，请检查账号是否正确");
        }
        
        // 检查角色是否为志愿者
        if (!"VOLUNTEER".equals(user.getRoleCode())) {
            return ApiResponse.fail("该账号不是志愿者账号");
        }
        
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return ApiResponse.fail("密码错误");
        }
        
        // 查找志愿者记录（如果还没查过）
        if (volunteer == null) {
            volunteer = volunteerMapper.selectOne(new LambdaQueryWrapper<Volunteer>()
                    .eq(Volunteer::getUserId, user.getId()));
            if (volunteer == null) {
                volunteer = volunteerMapper.selectOne(new LambdaQueryWrapper<Volunteer>()
                        .eq(Volunteer::getPhone, user.getUsername()));
            }
        }
        
        // 检查志愿者状态
        if (volunteer == null) {
            return ApiResponse.fail("您还不是志愿者，请先申请成为志愿者");
        }
        if ("rejected".equals(volunteer.getStatus())) {
            return ApiResponse.fail("审核未通过，无法登录");
        }
        if ("pending".equals(volunteer.getStatus())) {
            return ApiResponse.fail("账号审核中，请稍后再试");
        }
        
        if (!Boolean.TRUE.equals(user.getEnabled())) {
            return ApiResponse.fail("账号已禁用");
        }
        
        String token = jwtUtil.generateToken(user.getUsername(), user.getRoleCode());
        return ApiResponse.ok(new LoginResponse(token, user.getUsername(), user.getRoleCode()));
    }

    @GetMapping("/auth/check-phone")
    public ApiResponse<java.util.Map<String, Object>> checkPhone(@RequestParam String phone) {
        boolean valid = phone != null && phone.matches(PHONE_REGEX);
        boolean exists = valid && userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getUsername, phone)) > 0;
        String status = null;
        if (valid) {
            Volunteer volunteer = volunteerMapper.selectOne(new LambdaQueryWrapper<Volunteer>()
                    .eq(Volunteer::getPhone, phone));
            if (volunteer != null) {
                status = volunteer.getStatus();
            }
        }
        java.util.Map<String, Object> map = new java.util.HashMap<>();
        map.put("exists", exists);
        map.put("valid", valid);
        map.put("status", status);
        return ApiResponse.ok(map);
    }

    @PostMapping("/auth/reset-password")
    public ApiResponse<Void> resetPassword(@Valid @RequestBody PortalResetPasswordRequest request) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, request.getPhone()));
        Assert.notNull(user, "用户不存在");
        Assert.isTrue("VOLUNTEER".equals(user.getRoleCode()), "账号类型不匹配");
        Volunteer volunteer = volunteerMapper.selectOne(new LambdaQueryWrapper<Volunteer>()
                .eq(Volunteer::getUserId, user.getId()));
        if (volunteer != null && !"approved".equals(volunteer.getStatus())) {
            return ApiResponse.fail("账号未审核通过，无法重置密码");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
        return ApiResponse.ok(null);
    }

    @PostMapping("/auth/change-password")
    public ApiResponse<Void> changePassword(@Valid @RequestBody PortalChangePasswordRequest request) {
        User user = requirePortalUser();
        Assert.isTrue(passwordEncoder.matches(request.getOldPassword(), user.getPassword()), "原密码错误");
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
        return ApiResponse.ok(null);
    }

    @GetMapping("/me")
    public ApiResponse<PortalProfileDto> me() {
        User user = requirePortalUser();
        Volunteer volunteer = ensureVolunteer(user);
        return ApiResponse.ok(toProfile(volunteer, user));
    }

    @PutMapping("/me")
    public ApiResponse<PortalProfileDto> update(@RequestBody PortalProfileRequest request) {
        User user = requirePortalUser();
        Volunteer volunteer = ensureVolunteer(user);
        if (request.getName() != null) {
            volunteer.setName(request.getName());
            user.setNickname(request.getName());
        }
        if (request.getEmail() != null) {
            volunteer.setEmail(request.getEmail());
        }
        if (request.getOrganization() != null) {
            volunteer.setOrganization(request.getOrganization());
        }
        volunteer.setUpdatedAt(LocalDateTime.now());
        volunteerMapper.updateById(volunteer);
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
        return ApiResponse.ok(toProfile(volunteer, user));
    }

    @GetMapping("/my-signups")
    public ApiResponse<List<VolunteerSignupDto>> mySignups() {
        User user = requirePortalUser();
        Volunteer volunteer = ensureVolunteer(user);
        List<ActivitySignup> signups = activitySignupMapper.selectList(new LambdaQueryWrapper<ActivitySignup>()
                .eq(ActivitySignup::getVolunteerId, volunteer.getId()));
        List<VolunteerSignupDto> list = signups.stream().map(s -> {
            Activity a = activityMapper.selectById(s.getActivityId());
            VolunteerSignupDto dto = new VolunteerSignupDto();
            dto.setActivityId(s.getActivityId());
            dto.setTitle(a != null ? a.getTitle() : null);
            dto.setLocation(a != null ? a.getLocation() : null);
            dto.setStartTime(a != null ? a.getStartTime() : null);
            dto.setEndTime(a != null ? a.getEndTime() : null);
            dto.setStatus(s.getStatus());
            dto.setCheckinTime(s.getCheckinTime());
            dto.setSignupTime(s.getCreatedAt());
            return dto;
        }).collect(Collectors.toList());
        return ApiResponse.ok(list);
    }

    @GetMapping("/audit-logs")
    public ApiResponse<List<VolunteerStatusLog>> auditLogs() {
        User user = requirePortalUser();
        Volunteer volunteer = ensureVolunteer(user);
        List<VolunteerStatusLog> logs = volunteerStatusLogMapper.selectList(
                new LambdaQueryWrapper<VolunteerStatusLog>()
                        .eq(VolunteerStatusLog::getVolunteerId, volunteer.getId())
                        .orderByDesc(VolunteerStatusLog::getCreatedAt)
        );
        return ApiResponse.ok(logs);
    }

    @GetMapping("/stats")
    public ApiResponse<java.util.Map<String, Object>> stats() {
        User user = requirePortalUser();
        Volunteer volunteer = ensureVolunteer(user);
        List<ActivitySignup> signups = activitySignupMapper.selectList(new LambdaQueryWrapper<ActivitySignup>()
                .eq(ActivitySignup::getVolunteerId, volunteer.getId()));
        long total = signups.size();
        long checkedIn = signups.stream().filter(s -> "checked_in".equals(s.getStatus())).count();
        long applied = signups.stream().filter(s -> "applied".equals(s.getStatus())).count();
        long upcoming = signups.stream()
                .map(s -> activityMapper.selectById(s.getActivityId()))
                .filter(a -> a != null && a.getStartTime() != null && a.getStartTime().isAfter(LocalDateTime.now()))
                .filter(a -> a.getStartTime().isBefore(LocalDateTime.now().plusDays(7)))
                .count();
        java.util.Map<String, Object> map = new java.util.HashMap<>();
        map.put("total", total);
        map.put("checkedIn", checkedIn);
        map.put("applied", applied);
        map.put("upcoming", upcoming);
        return ApiResponse.ok(map);
    }

    @GetMapping("/reminder-settings")
    public ApiResponse<VolunteerReminderSetting> reminderSettings() {
        User user = requirePortalUser();
        Volunteer volunteer = ensureVolunteer(user);
        return ApiResponse.ok(getOrCreateReminderSetting(volunteer));
    }

    @PutMapping("/reminder-settings")
    public ApiResponse<VolunteerReminderSetting> updateReminderSettings(@RequestBody PortalReminderSettingRequest request) {
        User user = requirePortalUser();
        Volunteer volunteer = ensureVolunteer(user);
        VolunteerReminderSetting setting = getOrCreateReminderSetting(volunteer);
        if (request.getSignupReminder() != null) {
            setting.setSignupReminder(request.getSignupReminder());
        }
        if (request.getCheckinReminder() != null) {
            setting.setCheckinReminder(request.getCheckinReminder());
        }
        if (request.getChannel() != null) {
            setting.setChannel(request.getChannel());
        }
        if (request.getReminderMinutes() != null) {
            setting.setReminderMinutes(request.getReminderMinutes());
        }
        setting.setUpdatedAt(LocalDateTime.now());
        reminderSettingMapper.updateById(setting);
        return ApiResponse.ok(setting);
    }

    @GetMapping("/reminders")
    public ApiResponse<List<PortalReminderDto>> reminders() {
        User user = requirePortalUser();
        Volunteer volunteer = ensureVolunteer(user);
        VolunteerReminderSetting setting = getOrCreateReminderSetting(volunteer);
        if (!Boolean.TRUE.equals(setting.getSignupReminder())) {
            return ApiResponse.ok(List.of());
        }
        LocalDateTime now = LocalDateTime.now();
        List<ActivitySignup> signups = activitySignupMapper.selectList(new LambdaQueryWrapper<ActivitySignup>()
                .eq(ActivitySignup::getVolunteerId, volunteer.getId()));
        List<PortalReminderDto> list = signups.stream()
                .map(s -> activityMapper.selectById(s.getActivityId()))
                .filter(a -> a != null && a.getStartTime() != null && a.getStartTime().isAfter(now))
                .filter(a -> a.getStartTime().isBefore(now.plusDays(7)))
                .sorted((a, b) -> a.getStartTime().compareTo(b.getStartTime()))
                .map(a -> {
                    PortalReminderDto dto = new PortalReminderDto();
                    dto.setActivityId(a.getId());
                    dto.setTitle(a.getTitle());
                    dto.setLocation(a.getLocation());
                    dto.setStartTime(a.getStartTime());
                    dto.setCountdownSeconds(Duration.between(now, a.getStartTime()).getSeconds());
                    return dto;
                })
                .collect(Collectors.toList());
        return ApiResponse.ok(list);
    }

    @GetMapping("/reminder-logs")
    public ApiResponse<List<PortalReminderLogDto>> reminderLogs() {
        User user = requirePortalUser();
        Volunteer volunteer = ensureVolunteer(user);
        List<ActivityReminderLog> logs = activityReminderLogMapper.selectList(
                new LambdaQueryWrapper<ActivityReminderLog>()
                        .eq(ActivityReminderLog::getVolunteerId, volunteer.getId())
                        .orderByDesc(ActivityReminderLog::getCreatedAt)
                        .last("limit 50"));
        List<PortalReminderLogDto> list = logs.stream().map(log -> {
            PortalReminderLogDto dto = new PortalReminderLogDto();
            dto.setId(log.getId());
            dto.setActivityId(log.getActivityId());
            dto.setReminderType(log.getReminderType());
            dto.setChannel(log.getChannel());
            dto.setStatus(log.getStatus());
            dto.setMessage(log.getMessage());
            dto.setCreatedAt(log.getCreatedAt());
            Activity activity = activityMapper.selectById(log.getActivityId());
            dto.setActivityTitle(activity != null ? activity.getTitle() : null);
            return dto;
        }).collect(Collectors.toList());
        return ApiResponse.ok(list);
    }

    @GetMapping("/reminder-logs/export")
    public ResponseEntity<byte[]> exportReminderLogs(@RequestParam(required = false) String startTime,
                                                     @RequestParam(required = false) String endTime) {
        User user = requirePortalUser();
        Volunteer volunteer = ensureVolunteer(user);
        List<ActivityReminderLog> logs = activityReminderLogMapper.selectList(
                new LambdaQueryWrapper<ActivityReminderLog>()
                        .eq(ActivityReminderLog::getVolunteerId, volunteer.getId())
                        .ge(startTime != null && !startTime.isEmpty(), ActivityReminderLog::getCreatedAt, parseTime(startTime))
                        .le(endTime != null && !endTime.isEmpty(), ActivityReminderLog::getCreatedAt, parseTime(endTime))
                        .orderByDesc(ActivityReminderLog::getCreatedAt));
        Map<Long, Activity> activityMap = loadActivities(logs.stream()
                .map(ActivityReminderLog::getActivityId)
                .collect(Collectors.toSet()));
        StringBuilder sb = new StringBuilder();
        sb.append("时间,活动,通道,状态,内容\n");
        for (ActivityReminderLog log : logs) {
            Activity activity = activityMap.get(log.getActivityId());
            sb.append(formatCsv(log.getCreatedAt()))
                    .append(",")
                    .append(activity != null ? activity.getTitle() : "")
                    .append(",")
                    .append(log.getChannel() != null ? log.getChannel() : "")
                    .append(",")
                    .append(log.getStatus() != null ? log.getStatus() : "")
                    .append(",")
                    .append(formatCsv(log.getMessage()))
                    .append("\n");
        }
        byte[] bytes = sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=portal-reminder-logs.csv")
                .contentType(MediaType.TEXT_PLAIN)
                .contentLength(bytes.length)
                .body(bytes);
    }

    @GetMapping("/messages")
    public ApiResponse<Page<PortalMessageDto>> messages(@RequestParam(defaultValue = "1") int page,
                                                        @RequestParam(defaultValue = "10") int size,
                                                        @RequestParam(required = false) String type,
                                                        @RequestParam(required = false) String status,
                                                        @RequestParam(required = false) String read,
                                                        @RequestParam(required = false) String startTime,
                                                        @RequestParam(required = false) String endTime) {
        User user = requirePortalUser();
        Volunteer volunteer = ensureVolunteer(user);
        List<PortalMessageDto> messages = buildPortalMessages(volunteer);
        Map<String, PortalMessageRead> readMap = loadReadMap(volunteer.getId());
        messages.forEach(m -> m.setRead(readMap.containsKey(m.getKey())));

        if (type != null && !type.isEmpty() && !"all".equalsIgnoreCase(type)) {
            messages = messages.stream().filter(m -> type.equalsIgnoreCase(m.getType())).collect(Collectors.toList());
        }
        if (status != null && !status.isEmpty()) {
            messages = messages.stream().filter(m -> status.equalsIgnoreCase(m.getStatus())).collect(Collectors.toList());
        }
        LocalDateTime start = parseTime(startTime);
        LocalDateTime end = parseTime(endTime);
        if (start != null) {
            messages = messages.stream().filter(m -> m.getCreatedAt() != null && !m.getCreatedAt().isBefore(start)).collect(Collectors.toList());
        }
        if (end != null) {
            messages = messages.stream().filter(m -> m.getCreatedAt() != null && !m.getCreatedAt().isAfter(end)).collect(Collectors.toList());
        }
        Boolean readFilter = parseReadFilter(read);
        if (readFilter != null) {
            boolean isRead = readFilter;
            messages = messages.stream().filter(m -> Boolean.TRUE.equals(m.getRead()) == isRead).collect(Collectors.toList());
        }

        long total = messages.size();
        int from = Math.max(0, (page - 1) * size);
        int to = Math.min(messages.size(), from + size);
        List<PortalMessageDto> records = from >= messages.size() ? List.of() : messages.subList(from, to);
        Page<PortalMessageDto> result = new Page<>(page, size);
        result.setTotal(total);
        result.setRecords(records);
        return ApiResponse.ok(result);
    }

    @PostMapping("/messages/read")
    public ApiResponse<Void> markMessagesRead(@RequestBody PortalMessageReadRequest request) {
        User user = requirePortalUser();
        Volunteer volunteer = ensureVolunteer(user);
        List<String> keys = request != null ? request.getKeys() : null;
        if ((keys == null || keys.isEmpty()) && Boolean.TRUE.equals(request != null ? request.getReadAll() : null)) {
            keys = buildPortalMessages(volunteer).stream().map(PortalMessageDto::getKey).collect(Collectors.toList());
        }
        if (keys == null || keys.isEmpty()) {
            return ApiResponse.ok(null);
        }
        Map<String, PortalMessageRead> readMap = loadReadMap(volunteer.getId());
        for (String key : keys) {
            if (readMap.containsKey(key)) {
                continue;
            }
            PortalMessageRead readItem = new PortalMessageRead();
            readItem.setVolunteerId(volunteer.getId());
            readItem.setMessageKey(key);
            readItem.setReadAt(LocalDateTime.now());
            portalMessageReadMapper.insert(readItem);
        }
        return ApiResponse.ok(null);
    }

    @PostMapping("/activities/signup")
    public ApiResponse<ActivitySignup> signup(@Valid @RequestBody PortalActivitySignupRequest request) {
        User user = requirePortalUser();
        Volunteer volunteer = ensureVolunteer(user);
        Activity activity = activityMapper.selectById(request.getActivityId());
        Assert.notNull(activity, "活动不存在");
        if (!"approved".equals(volunteer.getStatus())) {
            return ApiResponse.fail("账号未审核通过，无法报名");
        }
        
        // 检查是否仅限组织成员
        if (Boolean.TRUE.equals(activity.getMembersOnly()) && activity.getOrgId() != null) {
            VolunteerOrgMember member = volunteerOrgMemberMapper.selectOne(
                    new LambdaQueryWrapper<VolunteerOrgMember>()
                            .eq(VolunteerOrgMember::getVolunteerId, volunteer.getId())
                            .eq(VolunteerOrgMember::getOrgId, activity.getOrgId())
                            .eq(VolunteerOrgMember::getStatus, "approved"));
            if (member == null) {
                return ApiResponse.fail("该活动仅限组织成员参与，请先加入对应组织");
            }
        }
        
        ActivitySignup existing = activitySignupMapper.selectOne(new LambdaQueryWrapper<ActivitySignup>()
                .eq(ActivitySignup::getActivityId, request.getActivityId())
                .eq(ActivitySignup::getVolunteerId, volunteer.getId()));
        if (existing != null) {
            return ApiResponse.ok(existing);
        }
        ActivitySignup signup = new ActivitySignup();
        signup.setActivityId(request.getActivityId());
        signup.setVolunteerId(volunteer.getId());
        signup.setStatus("applied");
        signup.setCreatedAt(LocalDateTime.now());
        activitySignupMapper.insert(signup);
        return ApiResponse.ok(signup);
    }

    @PostMapping("/checkin")
    public ApiResponse<ActivitySignup> checkin(@RequestBody java.util.Map<String, String> request) {
        User user = requirePortalUser();
        Volunteer volunteer = ensureVolunteer(user);
        String code = request.get("code");
        if (code == null || code.trim().isEmpty()) {
            return ApiResponse.fail("请输入签到码");
        }
        // 通过签到码查找活动
        Activity activity = activityMapper.selectOne(new LambdaQueryWrapper<Activity>()
                .eq(Activity::getCheckinCode, code.trim()));
        if (activity == null) {
            return ApiResponse.fail("签到码无效");
        }
        
        // 先检查是否已报名
        ActivitySignup signup = activitySignupMapper.selectOne(new LambdaQueryWrapper<ActivitySignup>()
                .eq(ActivitySignup::getActivityId, activity.getId())
                .eq(ActivitySignup::getVolunteerId, volunteer.getId()));
        if (signup == null) {
            return ApiResponse.fail("您尚未报名此活动，请先报名后再签到");
        }
        if ("checked_in".equals(signup.getStatus())) {
            return ApiResponse.fail("您已签到过此活动");
        }
        
        // 再检查活动时间范围
        LocalDateTime now = LocalDateTime.now();
        if (activity.getStartTime() != null && now.isBefore(activity.getStartTime())) {
            return ApiResponse.fail("活动尚未开始，无法签到");
        }
        if (activity.getEndTime() != null && now.isAfter(activity.getEndTime())) {
            return ApiResponse.fail("活动已结束，无法签到");
        }
        
        // 签到
        signup.setStatus("checked_in");
        signup.setCheckinTime(LocalDateTime.now());
        activitySignupMapper.updateById(signup);
        return ApiResponse.ok(signup);
    }

    @DeleteMapping("/activities/signup/{activityId}")
    public ApiResponse<Void> cancelSignup(@PathVariable Long activityId) {
        User user = requirePortalUser();
        Volunteer volunteer = ensureVolunteer(user);
        
        // 查找报名记录
        ActivitySignup signup = activitySignupMapper.selectOne(new LambdaQueryWrapper<ActivitySignup>()
                .eq(ActivitySignup::getActivityId, activityId)
                .eq(ActivitySignup::getVolunteerId, volunteer.getId()));
        
        if (signup == null) {
            return ApiResponse.fail("您未报名此活动");
        }
        
        // 已签到不能取消
        if ("checked_in".equals(signup.getStatus())) {
            return ApiResponse.fail("已签到的活动无法取消报名");
        }
        
        // 检查活动是否已开始
        Activity activity = activityMapper.selectById(activityId);
        if (activity != null && activity.getStartTime() != null 
                && LocalDateTime.now().isAfter(activity.getStartTime())) {
            return ApiResponse.fail("活动已开始，无法取消报名");
        }
        
        // 删除报名记录
        activitySignupMapper.deleteById(signup.getId());
        return ApiResponse.ok(null);
    }

    @GetMapping("/my-orgs")
    public ApiResponse<List<Map<String, Object>>> getMyOrgs() {
        User user = requirePortalUser();
        Volunteer volunteer = ensureVolunteer(user);
        
        List<VolunteerOrgMember> members = volunteerOrgMemberMapper.selectList(
                new LambdaQueryWrapper<VolunteerOrgMember>()
                        .eq(VolunteerOrgMember::getVolunteerId, volunteer.getId()));
        
        List<Map<String, Object>> result = members.stream().map(m -> {
            Map<String, Object> map = new HashMap<>();
            map.put("memberId", m.getId());
            map.put("status", m.getStatus());
            map.put("joinedAt", m.getJoinedAt());
            VolunteerOrg org = volunteerOrgMapper.selectById(m.getOrgId());
            if (org != null) {
                map.put("orgId", org.getId());
                map.put("orgName", org.getName());
                map.put("orgLogo", org.getLogoUrl());
            }
            return map;
        }).collect(Collectors.toList());
        
        return ApiResponse.ok(result);
    }

    @PostMapping("/join-org/{orgId}")
    public ApiResponse<Void> joinOrg(@PathVariable Long orgId) {
        User user = requirePortalUser();
        Volunteer volunteer = ensureVolunteer(user);
        
        // 检查志愿者状态
        if (!"approved".equals(volunteer.getStatus())) {
            return ApiResponse.fail("账号未审核通过，无法申请加入组织");
        }
        
        // 检查组织是否存在
        VolunteerOrg org = volunteerOrgMapper.selectById(orgId);
        if (org == null) {
            return ApiResponse.fail("组织不存在");
        }
        
        // 检查是否已申请
        VolunteerOrgMember existing = volunteerOrgMemberMapper.selectOne(
                new LambdaQueryWrapper<VolunteerOrgMember>()
                        .eq(VolunteerOrgMember::getVolunteerId, volunteer.getId())
                        .eq(VolunteerOrgMember::getOrgId, orgId));
        if (existing != null) {
            return ApiResponse.fail("您已申请加入该组织");
        }
        
        // 创建申请记录
        VolunteerOrgMember member = new VolunteerOrgMember();
        member.setVolunteerId(volunteer.getId());
        member.setOrgId(orgId);
        member.setStatus("pending");
        member.setCreatedAt(LocalDateTime.now());
        volunteerOrgMemberMapper.insert(member);
        
        return ApiResponse.ok(null);
    }

    @DeleteMapping("/leave-org/{orgId}")
    public ApiResponse<Void> leaveOrg(@PathVariable Long orgId) {
        User user = requirePortalUser();
        Volunteer volunteer = ensureVolunteer(user);
        
        // 查找成员记录
        VolunteerOrgMember member = volunteerOrgMemberMapper.selectOne(
                new LambdaQueryWrapper<VolunteerOrgMember>()
                        .eq(VolunteerOrgMember::getVolunteerId, volunteer.getId())
                        .eq(VolunteerOrgMember::getOrgId, orgId));
        
        if (member == null) {
            return ApiResponse.fail("您未加入该组织");
        }
        
        // 删除成员记录
        volunteerOrgMemberMapper.deleteById(member.getId());
        return ApiResponse.ok(null);
    }

    private User requirePortalUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Assert.notNull(auth, "未登录");
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, auth.getName()));
        Assert.notNull(user, "用户不存在");
        Assert.isTrue(user.getEnabled() != null && user.getEnabled(), "账号已被禁用");
        Assert.isTrue("VOLUNTEER".equals(user.getRoleCode()), "权限不足");
        return user;
    }

    private Volunteer ensureVolunteer(User user) {
        Volunteer volunteer = volunteerMapper.selectOne(new LambdaQueryWrapper<Volunteer>()
                .eq(Volunteer::getUserId, user.getId()));
        if (volunteer != null) {
            return volunteer;
        }
        volunteer = volunteerMapper.selectOne(new LambdaQueryWrapper<Volunteer>()
                .eq(Volunteer::getPhone, user.getUsername()));
        if (volunteer == null) {
            volunteer = new Volunteer();
            volunteer.setName(user.getNickname());
            volunteer.setPhone(user.getUsername());
            volunteer.setStatus("pending");
            volunteer.setUserId(user.getId());
            volunteer.setCreatedAt(LocalDateTime.now());
            volunteer.setUpdatedAt(LocalDateTime.now());
            volunteerMapper.insert(volunteer);
            logStatus(volunteer.getId(), volunteer.getStatus(), "账号自动补全");
        } else {
            volunteer.setUserId(user.getId());
            volunteer.setUpdatedAt(LocalDateTime.now());
            volunteerMapper.updateById(volunteer);
        }
        return volunteer;
    }

    private VolunteerReminderSetting getOrCreateReminderSetting(Volunteer volunteer) {
        VolunteerReminderSetting setting = reminderSettingMapper.selectOne(
                new LambdaQueryWrapper<VolunteerReminderSetting>().eq(VolunteerReminderSetting::getVolunteerId, volunteer.getId()));
        if (setting != null) {
            return setting;
        }
        setting = new VolunteerReminderSetting();
        setting.setVolunteerId(volunteer.getId());
        setting.setSignupReminder(true);
        setting.setCheckinReminder(true);
        if (volunteer.getPhone() != null && !volunteer.getPhone().isEmpty()) {
            setting.setChannel("sms");
        } else if (volunteer.getEmail() != null && !volunteer.getEmail().isEmpty()) {
            setting.setChannel("email");
        } else {
            setting.setChannel("web");
        }
        setting.setReminderMinutes(30);
        setting.setCreatedAt(LocalDateTime.now());
        setting.setUpdatedAt(LocalDateTime.now());
        reminderSettingMapper.insert(setting);
        return setting;
    }

    private PortalProfileDto toProfile(Volunteer volunteer, User user) {
        PortalProfileDto dto = new PortalProfileDto();
        dto.setId(volunteer.getId());
        dto.setUserId(volunteer.getUserId());
        dto.setName(volunteer.getName());
        dto.setPhone(volunteer.getPhone());
        dto.setEmail(volunteer.getEmail());
        dto.setOrganization(volunteer.getOrganization());
        dto.setStatus(volunteer.getStatus());
        dto.setEnabled(user != null ? user.getEnabled() : true);
        dto.setCreatedAt(volunteer.getCreatedAt());
        dto.setUpdatedAt(volunteer.getUpdatedAt());
        return dto;
    }

    private void logStatus(Long volunteerId, String status, String remark) {
        VolunteerStatusLog log = new VolunteerStatusLog();
        log.setVolunteerId(volunteerId);
        log.setStatus(status);
        log.setRemark(remark);
        log.setCreatedAt(LocalDateTime.now());
        volunteerStatusLogMapper.insert(log);
    }

    private List<PortalMessageDto> buildPortalMessages(Volunteer volunteer) {
        List<ActivitySignup> signups = activitySignupMapper.selectList(new LambdaQueryWrapper<ActivitySignup>()
                .eq(ActivitySignup::getVolunteerId, volunteer.getId()));
        // 获取站内消息（不再从 ActivityReminderLog 读取，避免重复）
        List<VolunteerMessage> volunteerMessages = volunteerMessageMapper.selectList(
                new LambdaQueryWrapper<VolunteerMessage>()
                        .eq(VolunteerMessage::getVolunteerId, volunteer.getId())
                        .orderByDesc(VolunteerMessage::getCreatedAt)
                        .last("limit 200"));
        
        Set<Long> activityIds = new HashSet<>();
        signups.forEach(s -> activityIds.add(s.getActivityId()));
        volunteerMessages.forEach(m -> { if (m.getActivityId() != null) activityIds.add(m.getActivityId()); });
        Map<Long, Activity> activityMap = loadActivities(activityIds);

        List<PortalMessageDto> list = new ArrayList<>();
        for (ActivitySignup s : signups) {
            Activity activity = activityMap.get(s.getActivityId());
            String title = activity != null ? activity.getTitle() : "活动";
            if (s.getCreatedAt() != null) {
                PortalMessageDto dto = new PortalMessageDto();
                dto.setKey("signup:" + s.getId());
                dto.setType("signup");
                dto.setTitle("报名活动 · " + title);
                dto.setStatus(s.getStatus());
                dto.setMessage(activity != null ? activity.getLocation() : null);
                dto.setCreatedAt(s.getCreatedAt());
                list.add(dto);
            }
            if (s.getCheckinTime() != null) {
                PortalMessageDto dto = new PortalMessageDto();
                dto.setKey("checkin:" + s.getId());
                dto.setType("checkin");
                dto.setTitle("签到成功 · " + title);
                dto.setStatus("checked_in");
                dto.setMessage(activity != null ? activity.getLocation() : null);
                dto.setCreatedAt(s.getCheckinTime());
                list.add(dto);
            }
        }
        // 添加站内消息
        for (VolunteerMessage msg : volunteerMessages) {
            PortalMessageDto dto = new PortalMessageDto();
            dto.setKey("message:" + msg.getId());
            dto.setType(msg.getType() != null ? msg.getType() : "reminder");
            dto.setTitle(msg.getTitle());
            dto.setStatus(Boolean.TRUE.equals(msg.getIsRead()) ? "read" : "unread");
            dto.setMessage(msg.getContent());
            dto.setCreatedAt(msg.getCreatedAt());
            list.add(dto);
        }
        return list.stream()
                .sorted((a, b) -> {
                    LocalDateTime ta = a.getCreatedAt();
                    LocalDateTime tb = b.getCreatedAt();
                    if (ta == null && tb == null) return 0;
                    if (ta == null) return 1;
                    if (tb == null) return -1;
                    return tb.compareTo(ta);
                })
                .collect(Collectors.toList());
    }

    private Map<String, PortalMessageRead> loadReadMap(Long volunteerId) {
        List<PortalMessageRead> reads = portalMessageReadMapper.selectList(
                new LambdaQueryWrapper<PortalMessageRead>().eq(PortalMessageRead::getVolunteerId, volunteerId));
        Map<String, PortalMessageRead> map = new HashMap<>();
        for (PortalMessageRead read : reads) {
            map.put(read.getMessageKey(), read);
        }
        return map;
    }

    private Map<Long, Activity> loadActivities(Set<Long> ids) {
        Map<Long, Activity> map = new HashMap<>();
        if (ids == null || ids.isEmpty()) {
            return map;
        }
        List<Activity> activities = activityMapper.selectBatchIds(ids);
        for (Activity a : activities) {
            map.put(a.getId(), a);
        }
        return map;
    }

    private Boolean parseReadFilter(String read) {
        if (read == null || read.isEmpty() || "all".equalsIgnoreCase(read)) {
            return null;
        }
        if ("read".equalsIgnoreCase(read) || "true".equalsIgnoreCase(read)) {
            return true;
        }
        if ("unread".equalsIgnoreCase(read) || "false".equalsIgnoreCase(read)) {
            return false;
        }
        return null;
    }

    private String formatCsv(Object value) {
        if (value == null) {
            return "";
        }
        String text = value.toString().replace("T", " ");
        if (text.contains(",") || text.contains("\"") || text.contains("\n")) {
            text = "\"" + text.replace("\"", "\"\"") + "\"";
        }
        return text;
    }

    private LocalDateTime parseTime(String val) {
        if (val == null || val.isEmpty()) {
            return null;
        }
        try {
            return LocalDateTime.parse(val.replace(" ", "T"));
        } catch (Exception e) {
            return null;
        }
    }
}

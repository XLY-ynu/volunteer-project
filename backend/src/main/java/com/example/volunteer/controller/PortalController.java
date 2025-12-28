package com.example.volunteer.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.volunteer.common.ApiResponse;
import com.example.volunteer.dto.LoginResponse;
import com.example.volunteer.dto.PortalActivitySignupRequest;
import com.example.volunteer.dto.PortalChangePasswordRequest;
import com.example.volunteer.dto.PortalLoginRequest;
import com.example.volunteer.dto.PortalProfileDto;
import com.example.volunteer.dto.PortalProfileRequest;
import com.example.volunteer.dto.PortalReminderDto;
import com.example.volunteer.dto.PortalReminderSettingRequest;
import com.example.volunteer.dto.PortalRegisterRequest;
import com.example.volunteer.dto.PortalResetPasswordRequest;
import com.example.volunteer.dto.VolunteerSignupDto;
import com.example.volunteer.entity.Activity;
import com.example.volunteer.entity.ActivitySignup;
import com.example.volunteer.entity.User;
import com.example.volunteer.entity.Volunteer;
import com.example.volunteer.entity.VolunteerReminderSetting;
import com.example.volunteer.entity.VolunteerStatusLog;
import com.example.volunteer.mapper.ActivityMapper;
import com.example.volunteer.mapper.ActivitySignupMapper;
import com.example.volunteer.mapper.UserMapper;
import com.example.volunteer.mapper.VolunteerMapper;
import com.example.volunteer.mapper.VolunteerReminderSettingMapper;
import com.example.volunteer.mapper.VolunteerStatusLogMapper;
import com.example.volunteer.security.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.List;
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
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public PortalController(UserMapper userMapper,
                            VolunteerMapper volunteerMapper,
                            ActivityMapper activityMapper,
                            ActivitySignupMapper activitySignupMapper,
                            VolunteerStatusLogMapper volunteerStatusLogMapper,
                            VolunteerReminderSettingMapper reminderSettingMapper,
                            PasswordEncoder passwordEncoder,
                            JwtUtil jwtUtil) {
        this.userMapper = userMapper;
        this.volunteerMapper = volunteerMapper;
        this.activityMapper = activityMapper;
        this.activitySignupMapper = activitySignupMapper;
        this.volunteerStatusLogMapper = volunteerStatusLogMapper;
        this.reminderSettingMapper = reminderSettingMapper;
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
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, request.getPhone()));
        Assert.notNull(user, "用户不存在");
        Assert.isTrue(passwordEncoder.matches(request.getPassword(), user.getPassword()), "密码错误");
        Assert.isTrue("VOLUNTEER".equals(user.getRoleCode()), "该账号无法登录志愿者端");
        Volunteer volunteer = volunteerMapper.selectOne(new LambdaQueryWrapper<Volunteer>()
                .eq(Volunteer::getUserId, user.getId()));
        if (volunteer == null) {
            volunteer = volunteerMapper.selectOne(new LambdaQueryWrapper<Volunteer>()
                    .eq(Volunteer::getPhone, user.getUsername()));
        }
        if (volunteer != null && "rejected".equals(volunteer.getStatus())) {
            return ApiResponse.fail("审核未通过，无法登录");
        }
        if (volunteer != null && "pending".equals(volunteer.getStatus())) {
            return ApiResponse.fail("账号审核中，请稍后再试");
        }
        Assert.isTrue(Boolean.TRUE.equals(user.getEnabled()), "账号已禁用");
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
        return ApiResponse.ok(toProfile(volunteer));
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
        return ApiResponse.ok(toProfile(volunteer));
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

    @PostMapping("/activities/signup")
    public ApiResponse<ActivitySignup> signup(@Valid @RequestBody PortalActivitySignupRequest request) {
        User user = requirePortalUser();
        Volunteer volunteer = ensureVolunteer(user);
        Activity activity = activityMapper.selectById(request.getActivityId());
        Assert.notNull(activity, "活动不存在");
        if (!"approved".equals(volunteer.getStatus())) {
            return ApiResponse.fail("账号未审核通过，无法报名");
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

    private User requirePortalUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Assert.notNull(auth, "未登录");
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, auth.getName()));
        Assert.notNull(user, "用户不存在");
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

    private PortalProfileDto toProfile(Volunteer volunteer) {
        PortalProfileDto dto = new PortalProfileDto();
        dto.setId(volunteer.getId());
        dto.setUserId(volunteer.getUserId());
        dto.setName(volunteer.getName());
        dto.setPhone(volunteer.getPhone());
        dto.setEmail(volunteer.getEmail());
        dto.setOrganization(volunteer.getOrganization());
        dto.setStatus(volunteer.getStatus());
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
}

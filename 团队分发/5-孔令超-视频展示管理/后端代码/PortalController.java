/**
 * @Author: 孔令超
 * @Module: 活动参与模块
 * @Description: 志愿者端控制器，支持登录/注册、个人信息、参与记录、活动报名与签到
 */
package com.example.volunteer.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.volunteer.common.ApiResponse;
import com.example.volunteer.dto.LoginResponse;
import com.example.volunteer.dto.PortalActivitySignupRequest;
import com.example.volunteer.dto.PortalLoginRequest;
import com.example.volunteer.dto.PortalProfileDto;
import com.example.volunteer.dto.PortalRegisterRequest;
import com.example.volunteer.dto.VolunteerSignupDto;
import com.example.volunteer.entity.Activity;
import com.example.volunteer.entity.ActivitySignup;
import com.example.volunteer.entity.User;
import com.example.volunteer.entity.Volunteer;
import com.example.volunteer.mapper.ActivityMapper;
import com.example.volunteer.mapper.ActivitySignupMapper;
import com.example.volunteer.mapper.UserMapper;
import com.example.volunteer.mapper.VolunteerMapper;
import com.example.volunteer.security.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/portal")
public class PortalController {

    private static final String PHONE_REGEX = "^1[3-9]\\d{9}$";
    private final UserMapper userMapper;
    private final VolunteerMapper volunteerMapper;
    private final ActivityMapper activityMapper;
    private final ActivitySignupMapper activitySignupMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public PortalController(UserMapper userMapper,
                            VolunteerMapper volunteerMapper,
                            ActivityMapper activityMapper,
                            ActivitySignupMapper activitySignupMapper,
                            PasswordEncoder passwordEncoder,
                            JwtUtil jwtUtil) {
        this.userMapper = userMapper;
        this.volunteerMapper = volunteerMapper;
        this.activityMapper = activityMapper;
        this.activitySignupMapper = activitySignupMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    // ========== 账号登录/注册 ==========

    @PostMapping("/auth/register")
    public ApiResponse<LoginResponse> register(@Valid @RequestBody PortalRegisterRequest request) {
        User existingUser = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, request.getPhone()));
        Assert.isNull(existingUser, "手机号已注册，请直接登录");

        User user = new User();
        user.setUsername(request.getPhone());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getName());
        user.setRoleCode("VOLUNTEER");
        user.setEnabled(false); // 待审核
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.insert(user);

        Volunteer volunteer = new Volunteer();
        volunteer.setName(request.getName());
        volunteer.setPhone(request.getPhone());
        volunteer.setEmail(request.getEmail());
        volunteer.setOrganization(request.getOrganization());
        volunteer.setStatus("pending");
        volunteer.setUserId(user.getId());
        volunteer.setCreatedAt(LocalDateTime.now());
        volunteer.setUpdatedAt(LocalDateTime.now());
        volunteerMapper.insert(volunteer);

        return ApiResponse.ok(new LoginResponse("", user.getUsername(), user.getRoleCode()));
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

    // ========== 个人信息 ==========

    @GetMapping("/me")
    public ApiResponse<PortalProfileDto> me() {
        User user = requirePortalUser();
        Volunteer volunteer = ensureVolunteer(user);
        return ApiResponse.ok(toProfile(volunteer, user));
    }

    @PutMapping("/me")
    public ApiResponse<PortalProfileDto> update(@RequestBody Map<String, String> request) {
        User user = requirePortalUser();
        Volunteer volunteer = ensureVolunteer(user);
        if (request.get("name") != null) {
            volunteer.setName(request.get("name"));
            user.setNickname(request.get("name"));
        }
        if (request.get("email") != null) {
            volunteer.setEmail(request.get("email"));
        }
        if (request.get("organization") != null) {
            volunteer.setOrganization(request.get("organization"));
        }
        volunteer.setUpdatedAt(LocalDateTime.now());
        volunteerMapper.updateById(volunteer);
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
        return ApiResponse.ok(toProfile(volunteer, user));
    }

    // ========== 参与记录 ==========

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

    @GetMapping("/stats")
    public ApiResponse<Map<String, Object>> stats() {
        User user = requirePortalUser();
        Volunteer volunteer = ensureVolunteer(user);
        List<ActivitySignup> signups = activitySignupMapper.selectList(new LambdaQueryWrapper<ActivitySignup>()
                .eq(ActivitySignup::getVolunteerId, volunteer.getId()));
        long total = signups.size();
        long checkedIn = signups.stream().filter(s -> "checked_in".equals(s.getStatus())).count();
        long applied = signups.stream().filter(s -> "applied".equals(s.getStatus())).count();
        
        Map<String, Object> map = new HashMap<>();
        map.put("total", total);
        map.put("checkedIn", checkedIn);
        map.put("applied", applied);
        return ApiResponse.ok(map);
    }

    // ========== 活动报名与签到 ==========

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

    @PostMapping("/checkin")
    public ApiResponse<ActivitySignup> checkin(@RequestBody Map<String, String> request) {
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
        // 查找报名记录
        ActivitySignup signup = activitySignupMapper.selectOne(new LambdaQueryWrapper<ActivitySignup>()
                .eq(ActivitySignup::getActivityId, activity.getId())
                .eq(ActivitySignup::getVolunteerId, volunteer.getId()));
        if (signup == null) {
            // 未报名，自动报名并签到
            signup = new ActivitySignup();
            signup.setActivityId(activity.getId());
            signup.setVolunteerId(volunteer.getId());
            signup.setStatus("checked_in");
            signup.setCheckinTime(LocalDateTime.now());
            signup.setCreatedAt(LocalDateTime.now());
            activitySignupMapper.insert(signup);
        } else if ("checked_in".equals(signup.getStatus())) {
            return ApiResponse.fail("您已签到过此活动");
        } else {
            signup.setStatus("checked_in");
            signup.setCheckinTime(LocalDateTime.now());
            activitySignupMapper.updateById(signup);
        }
        return ApiResponse.ok(signup);
    }

    @DeleteMapping("/activities/signup/{activityId}")
    public ApiResponse<Void> cancelSignup(@PathVariable Long activityId) {
        User user = requirePortalUser();
        Volunteer volunteer = ensureVolunteer(user);
        
        ActivitySignup signup = activitySignupMapper.selectOne(new LambdaQueryWrapper<ActivitySignup>()
                .eq(ActivitySignup::getActivityId, activityId)
                .eq(ActivitySignup::getVolunteerId, volunteer.getId()));
        
        if (signup == null) {
            return ApiResponse.fail("您未报名此活动");
        }
        if ("checked_in".equals(signup.getStatus())) {
            return ApiResponse.fail("已签到的活动无法取消报名");
        }
        
        Activity activity = activityMapper.selectById(activityId);
        if (activity != null && activity.getStartTime() != null 
                && LocalDateTime.now().isAfter(activity.getStartTime())) {
            return ApiResponse.fail("活动已开始，无法取消报名");
        }
        
        activitySignupMapper.deleteById(signup.getId());
        return ApiResponse.ok(null);
    }

    // ========== 辅助方法 ==========

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
        volunteer = new Volunteer();
        volunteer.setName(user.getNickname());
        volunteer.setPhone(user.getUsername());
        volunteer.setStatus("pending");
        volunteer.setUserId(user.getId());
        volunteer.setCreatedAt(LocalDateTime.now());
        volunteer.setUpdatedAt(LocalDateTime.now());
        volunteerMapper.insert(volunteer);
        return volunteer;
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
}

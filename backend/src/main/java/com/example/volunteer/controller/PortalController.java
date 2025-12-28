package com.example.volunteer.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.volunteer.common.ApiResponse;
import com.example.volunteer.dto.LoginResponse;
import com.example.volunteer.dto.PortalActivitySignupRequest;
import com.example.volunteer.dto.PortalLoginRequest;
import com.example.volunteer.dto.PortalProfileDto;
import com.example.volunteer.dto.PortalProfileRequest;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/portal")
public class PortalController {

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
        user.setEnabled(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.insert(user);

        Volunteer volunteer = volunteerMapper.selectOne(new LambdaQueryWrapper<Volunteer>()
                .eq(Volunteer::getPhone, request.getPhone()));
        if (volunteer == null) {
            volunteer = new Volunteer();
            volunteer.setName(request.getName());
            volunteer.setPhone(request.getPhone());
            volunteer.setEmail(request.getEmail());
            volunteer.setOrganization(request.getOrganization());
            volunteer.setStatus("pending");
            volunteer.setUserId(user.getId());
            volunteer.setCreatedAt(LocalDateTime.now());
            volunteer.setUpdatedAt(LocalDateTime.now());
            volunteerMapper.insert(volunteer);
        } else {
            volunteer.setUserId(user.getId());
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
        }

        String token = jwtUtil.generateToken(user.getUsername(), user.getRoleCode());
        return ApiResponse.ok(new LoginResponse(token, user.getUsername(), user.getRoleCode()));
    }

    @PostMapping("/auth/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody PortalLoginRequest request) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, request.getPhone()));
        Assert.notNull(user, "用户不存在");
        Assert.isTrue(Boolean.TRUE.equals(user.getEnabled()), "账号已禁用");
        Assert.isTrue(passwordEncoder.matches(request.getPassword(), user.getPassword()), "密码错误");
        Assert.isTrue("VOLUNTEER".equals(user.getRoleCode()), "该账号无法登录志愿者端");
        String token = jwtUtil.generateToken(user.getUsername(), user.getRoleCode());
        return ApiResponse.ok(new LoginResponse(token, user.getUsername(), user.getRoleCode()));
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

    @PostMapping("/activities/signup")
    public ApiResponse<ActivitySignup> signup(@Valid @RequestBody PortalActivitySignupRequest request) {
        User user = requirePortalUser();
        Volunteer volunteer = ensureVolunteer(user);
        Activity activity = activityMapper.selectById(request.getActivityId());
        Assert.notNull(activity, "活动不存在");
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
        } else {
            volunteer.setUserId(user.getId());
            volunteer.setUpdatedAt(LocalDateTime.now());
            volunteerMapper.updateById(volunteer);
        }
        return volunteer;
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
}

package com.example.volunteer.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.volunteer.common.ApiResponse;
import com.example.volunteer.entity.ContentItem;
import com.example.volunteer.entity.MenuCategory;
import com.example.volunteer.service.ContentService;
import com.example.volunteer.mapper.MenuCategoryMapper;
import com.example.volunteer.dto.TerminalPlaybackDto;
import com.example.volunteer.service.TerminalService;
import com.example.volunteer.entity.Activity;
import com.example.volunteer.entity.ActivitySignup;
import com.example.volunteer.entity.Volunteer;
import com.example.volunteer.mapper.ActivityMapper;
import com.example.volunteer.mapper.ActivitySignupMapper;
import com.example.volunteer.mapper.VolunteerMapper;
import java.time.LocalDateTime;
import com.example.volunteer.dto.ActivitySignupPublicRequest;
import com.example.volunteer.dto.ActivityCheckinPublicRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/public")
public class PublicController {

    private final MenuCategoryMapper menuCategoryMapper;
    private final ContentService contentService;
    private final TerminalService terminalService;
    private final ActivityMapper activityMapper;
    private final ActivitySignupMapper activitySignupMapper;
    private final VolunteerMapper volunteerMapper;

    public PublicController(MenuCategoryMapper menuCategoryMapper, ContentService contentService, TerminalService terminalService,
                            ActivityMapper activityMapper, ActivitySignupMapper activitySignupMapper, VolunteerMapper volunteerMapper) {
        this.menuCategoryMapper = menuCategoryMapper;
        this.contentService = contentService;
        this.terminalService = terminalService;
        this.activityMapper = activityMapper;
        this.activitySignupMapper = activitySignupMapper;
        this.volunteerMapper = volunteerMapper;
    }

    @GetMapping("/categories")
    public ApiResponse<List<MenuCategory>> categories(@RequestParam(required = false) Long parentId) {
        LambdaQueryWrapper<MenuCategory> w = new LambdaQueryWrapper<>();
        if (parentId != null) w.eq(MenuCategory::getParentId, parentId);
        w.orderByAsc(MenuCategory::getSortOrder);
        return ApiResponse.ok(menuCategoryMapper.selectList(w));
    }

    @GetMapping("/content")
    public ApiResponse<Page<ContentItem>> content(@RequestParam(defaultValue = "1") int page,
                                                  @RequestParam(defaultValue = "10") int size,
                                                  @RequestParam(required = false) Long categoryId,
                                                  @RequestParam(required = false) String keyword) {
        return ApiResponse.ok(contentService.page(page, size, categoryId, true, keyword));
    }

    @GetMapping("/playback")
    public ApiResponse<List<TerminalPlaybackDto>> playback(@RequestParam String terminalCode) {
        return ApiResponse.ok(terminalService.playbackForTerminal(terminalCode));
    }

    @GetMapping("/activities")
    public ApiResponse<Page<Activity>> activities(@RequestParam(defaultValue = "1") int page,
                                                  @RequestParam(defaultValue = "10") int size,
                                                  @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<Activity> w = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            w.like(Activity::getTitle, keyword).or().like(Activity::getDescription, keyword);
        }
        Page<Activity> p = new Page<>(page, size);
        activityMapper.selectPage(p, w);
        return ApiResponse.ok(p);
    }

    @PostMapping("/volunteer/register")
    public ApiResponse<Volunteer> registerVolunteer(@Valid @RequestBody Volunteer volunteer) {
        volunteer.setStatus("pending");
        volunteer.setCreatedAt(LocalDateTime.now());
        volunteer.setUpdatedAt(LocalDateTime.now());
        volunteerMapper.insert(volunteer);
        return ApiResponse.ok(volunteer);
    }

    @PostMapping("/activities/signup")
    public ApiResponse<ActivitySignup> activitySignup(@RequestBody ActivitySignup signup) {
        signup.setStatus("applied");
        signup.setCreatedAt(LocalDateTime.now());
        activitySignupMapper.insert(signup);
        return ApiResponse.ok(signup);
    }

    @PostMapping("/activities/signup-public")
    public ApiResponse<ActivitySignup> activitySignupPublic(@Valid @RequestBody ActivitySignupPublicRequest request) {
        Volunteer existing = null;
        if (request.getPhone() != null && !request.getPhone().isEmpty()) {
            existing = volunteerMapper.selectOne(new LambdaQueryWrapper<Volunteer>().eq(Volunteer::getPhone, request.getPhone()));
        }
        if (existing == null) {
            Volunteer v = new Volunteer();
            v.setName(request.getName());
            v.setPhone(request.getPhone());
            v.setEmail(request.getEmail());
            v.setOrganization(request.getOrganization());
            v.setStatus("pending");
            v.setCreatedAt(LocalDateTime.now());
            v.setUpdatedAt(LocalDateTime.now());
            volunteerMapper.insert(v);
            existing = v;
        }
        ActivitySignup signup = new ActivitySignup();
        signup.setActivityId(request.getActivityId());
        signup.setVolunteerId(existing.getId());
        signup.setStatus("applied");
        signup.setCreatedAt(LocalDateTime.now());
        activitySignupMapper.insert(signup);
        return ApiResponse.ok(signup);
    }

    @PostMapping("/activities/checkin")
    public ApiResponse<ActivitySignup> activityCheckin(@Valid @RequestBody ActivityCheckinPublicRequest request) {
        Activity activity = activityMapper.selectById(request.getActivityId());
        if (activity == null || activity.getCheckinCode() == null || !activity.getCheckinCode().equals(request.getCheckinCode())) {
            return ApiResponse.fail("签到码无效");
        }
        Volunteer volunteer = null;
        if (request.getPhone() != null && !request.getPhone().isEmpty()) {
            volunteer = volunteerMapper.selectOne(new LambdaQueryWrapper<Volunteer>().eq(Volunteer::getPhone, request.getPhone()));
        }
        if (volunteer == null) {
            volunteer = new Volunteer();
            volunteer.setName(request.getName());
            volunteer.setPhone(request.getPhone());
            volunteer.setEmail(request.getEmail());
            volunteer.setOrganization(request.getOrganization());
            volunteer.setStatus("pending");
            volunteer.setCreatedAt(LocalDateTime.now());
            volunteer.setUpdatedAt(LocalDateTime.now());
            volunteerMapper.insert(volunteer);
        }
        ActivitySignup signup = activitySignupMapper.selectOne(new LambdaQueryWrapper<ActivitySignup>()
                .eq(ActivitySignup::getActivityId, request.getActivityId())
                .eq(ActivitySignup::getVolunteerId, volunteer.getId()));
        if (signup == null) {
            signup = new ActivitySignup();
            signup.setActivityId(request.getActivityId());
            signup.setVolunteerId(volunteer.getId());
            signup.setCreatedAt(LocalDateTime.now());
            signup.setStatus("checked_in");
            signup.setCheckinTime(LocalDateTime.now());
            activitySignupMapper.insert(signup);
        } else {
            signup.setStatus("checked_in");
            signup.setCheckinTime(LocalDateTime.now());
            activitySignupMapper.updateById(signup);
        }
        return ApiResponse.ok(signup);
    }
}

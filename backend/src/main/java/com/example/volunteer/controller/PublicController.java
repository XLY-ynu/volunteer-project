package com.example.volunteer.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.volunteer.common.ApiResponse;
import com.example.volunteer.entity.ContentItem;
import com.example.volunteer.entity.MenuCategory;
import com.example.volunteer.dto.VolunteerSignupDto;
import com.example.volunteer.service.ContentService;
import com.example.volunteer.mapper.MenuCategoryMapper;
import com.example.volunteer.dto.TerminalPlaybackDto;
import com.example.volunteer.service.TerminalService;
import com.example.volunteer.entity.Activity;
import com.example.volunteer.entity.ActivitySignup;
import com.example.volunteer.entity.BroadcastJob;
import com.example.volunteer.entity.Volunteer;
import com.example.volunteer.mapper.ActivityMapper;
import com.example.volunteer.mapper.ActivitySignupMapper;
import com.example.volunteer.mapper.VolunteerMapper;
import com.example.volunteer.mapper.ContentConfigMapper;
import com.example.volunteer.mapper.ContentItemMapper;
import com.example.volunteer.mapper.MediaAssetMapper;
import com.example.volunteer.mapper.TerminalMapper;
import com.example.volunteer.mapper.VolunteerStatusLogMapper;
import com.example.volunteer.entity.Terminal;
import com.example.volunteer.entity.ContentConfig;
import com.example.volunteer.dto.TerminalPublicDto;
import com.example.volunteer.dto.BroadcastPlaybackDto;
import com.example.volunteer.entity.VolunteerStatusLog;
import com.example.volunteer.entity.MediaAsset;
import com.example.volunteer.service.BroadcastService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import com.example.volunteer.dto.ActivitySignupPublicRequest;
import com.example.volunteer.dto.ActivityCheckinPublicRequest;
import com.example.volunteer.dto.HeartbeatRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/public")
public class PublicController {

    private final MenuCategoryMapper menuCategoryMapper;
    private final ContentService contentService;
    private final TerminalService terminalService;
    private final ActivityMapper activityMapper;
    private final ActivitySignupMapper activitySignupMapper;
    private final VolunteerMapper volunteerMapper;
    private final ContentConfigMapper contentConfigMapper;
    private final TerminalMapper terminalMapper;
    private final VolunteerStatusLogMapper volunteerStatusLogMapper;
    private final BroadcastService broadcastService;
    private final MediaAssetMapper mediaAssetMapper;
    private final ContentItemMapper contentItemMapper;

    @Value("${app.terminal.offline-seconds:300}")
    private long offlineSeconds;

    public PublicController(MenuCategoryMapper menuCategoryMapper, ContentService contentService, TerminalService terminalService,
                            ActivityMapper activityMapper, ActivitySignupMapper activitySignupMapper,
                            VolunteerMapper volunteerMapper, ContentConfigMapper contentConfigMapper,
                            TerminalMapper terminalMapper,
                            VolunteerStatusLogMapper volunteerStatusLogMapper,
                            BroadcastService broadcastService,
                            MediaAssetMapper mediaAssetMapper,
                            ContentItemMapper contentItemMapper) {
        this.menuCategoryMapper = menuCategoryMapper;
        this.contentService = contentService;
        this.terminalService = terminalService;
        this.activityMapper = activityMapper;
        this.activitySignupMapper = activitySignupMapper;
        this.volunteerMapper = volunteerMapper;
        this.contentConfigMapper = contentConfigMapper;
        this.terminalMapper = terminalMapper;
        this.volunteerStatusLogMapper = volunteerStatusLogMapper;
        this.broadcastService = broadcastService;
        this.mediaAssetMapper = mediaAssetMapper;
        this.contentItemMapper = contentItemMapper;
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

    @GetMapping("/content-config")
    public ApiResponse<ContentConfig> contentConfig() {
        ContentConfig config = contentConfigMapper.selectOne(null);
        if (config == null) {
            config = new ContentConfig();
            config.setRecommendIntervalSec(6);
            config.setRecommendCount(6);
            config.setRecommendStrategy("prefer");
            config.setPreviewIntervalSec(10);
            config.setUpdatedAt(LocalDateTime.now());
            contentConfigMapper.insert(config);
        } else if (config.getRecommendStrategy() == null) {
            config.setRecommendStrategy("prefer");
            config.setUpdatedAt(LocalDateTime.now());
            contentConfigMapper.updateById(config);
        }
        return ApiResponse.ok(config);
    }

    @GetMapping("/recommendations")
    public ApiResponse<List<ContentItem>> recommendations(@RequestParam(required = false) Long parentId,
                                                          @RequestParam(required = false) Integer limit,
                                                          @RequestParam(required = false) String strategy) {
        if (strategy == null || strategy.isEmpty()) {
            ContentConfig config = contentConfigMapper.selectOne(null);
            strategy = config != null && config.getRecommendStrategy() != null ? config.getRecommendStrategy() : "prefer";
        }
        if ("global".equalsIgnoreCase(strategy)) {
            parentId = null;
        }
        List<ContentItem> all = contentService.listRecommended().stream()
                .filter(item -> Boolean.TRUE.equals(item.getPublished()))
                .collect(Collectors.toList());
        if (all.isEmpty()) {
            return ApiResponse.ok(List.of());
        }
        int take = resolveRecommendLimit(limit);
        if (parentId == null) {
            return ApiResponse.ok(limitList(all, take));
        }
        List<MenuCategory> children = menuCategoryMapper.selectList(
                new LambdaQueryWrapper<MenuCategory>().eq(MenuCategory::getParentId, parentId));
        Set<Long> categoryIds = new HashSet<>();
        categoryIds.add(parentId);
        for (MenuCategory c : children) {
            categoryIds.add(c.getId());
        }
        List<ContentItem> preferred = all.stream()
                .filter(item -> item.getCategoryId() != null && categoryIds.contains(item.getCategoryId()))
                .collect(Collectors.toList());
        if ("filter".equalsIgnoreCase(strategy)) {
            return ApiResponse.ok(limitList(preferred, take));
        }
        List<ContentItem> merged = new ArrayList<>(preferred);
        for (ContentItem item : all) {
            if (item.getCategoryId() != null && categoryIds.contains(item.getCategoryId())) {
                continue;
            }
            merged.add(item);
        }
        return ApiResponse.ok(limitList(merged, take));
    }

    @GetMapping("/terminals")
    public ApiResponse<List<TerminalPublicDto>> terminals(@RequestParam(required = false) String groupName) {
        LambdaQueryWrapper<Terminal> w = new LambdaQueryWrapper<>();
        if (groupName != null && !groupName.isEmpty()) {
            w.eq(Terminal::getGroupName, groupName);
        }
        List<Terminal> terminals = terminalMapper.selectList(w);
        LocalDateTime threshold = LocalDateTime.now().minusSeconds(offlineSeconds);
        List<TerminalPublicDto> list = terminals.stream().map(t -> {
            TerminalPublicDto dto = new TerminalPublicDto();
            dto.setCode(t.getCode());
            dto.setName(t.getName());
            dto.setGroupName(t.getGroupName());
            dto.setLastHeartbeat(t.getLastHeartbeat());
            String status = t.getStatus();
            if (t.getLastHeartbeat() != null && t.getLastHeartbeat().isBefore(threshold)) {
                status = "offline";
            }
            dto.setStatus(status);
            return dto;
        }).collect(Collectors.toList());
        return ApiResponse.ok(list);
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
        // 检查手机号是否已存在
        if (volunteer.getPhone() != null && !volunteer.getPhone().isEmpty()) {
            Volunteer existing = volunteerMapper.selectOne(new LambdaQueryWrapper<Volunteer>()
                    .eq(Volunteer::getPhone, volunteer.getPhone()));
            if (existing != null) {
                return ApiResponse.fail("该手机号已注册，请直接签到或联系管理员");
            }
        }
        volunteer.setStatus("pending");
        volunteer.setCreatedAt(LocalDateTime.now());
        volunteer.setUpdatedAt(LocalDateTime.now());
        volunteerMapper.insert(volunteer);
        logStatus(volunteer.getId(), volunteer.getStatus(), "游客注册");
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
            logStatus(v.getId(), v.getStatus(), "活动报名自动注册");
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
        // 通过签到码查找活动
        if (request.getCheckinCode() == null || request.getCheckinCode().trim().isEmpty()) {
            return ApiResponse.fail("请输入签到码");
        }
        
        Activity activity = activityMapper.selectOne(new LambdaQueryWrapper<Activity>()
                .eq(Activity::getCheckinCode, request.getCheckinCode().trim()));
        
        if (activity == null) {
            return ApiResponse.fail("签到码无效，请检查后重试");
        }
        
        // 必须通过姓名+手机号匹配已注册的志愿者
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            return ApiResponse.fail("请输入姓名");
        }
        if (request.getPhone() == null || request.getPhone().trim().isEmpty()) {
            return ApiResponse.fail("请输入手机号");
        }
        
        // 查找已注册的志愿者（姓名+手机号匹配）
        Volunteer volunteer = volunteerMapper.selectOne(new LambdaQueryWrapper<Volunteer>()
                .eq(Volunteer::getName, request.getName().trim())
                .eq(Volunteer::getPhone, request.getPhone().trim()));
        
        if (volunteer == null) {
            return ApiResponse.fail("签到失败，未找到您的志愿者信息，请先完成注册");
        }
        
        // 检查志愿者是否已通过审核
        if (!"approved".equals(volunteer.getStatus())) {
            if ("pending".equals(volunteer.getStatus())) {
                return ApiResponse.fail("您的志愿者申请正在审核中，请等待管理员审核通过后再签到");
            } else if ("rejected".equals(volunteer.getStatus())) {
                return ApiResponse.fail("您的志愿者申请已被拒绝，无法签到");
            } else {
                return ApiResponse.fail("您的志愿者状态异常，请联系管理员");
            }
        }
        
        // 检查是否已报名/签到
        ActivitySignup signup = activitySignupMapper.selectOne(new LambdaQueryWrapper<ActivitySignup>()
                .eq(ActivitySignup::getActivityId, activity.getId())
                .eq(ActivitySignup::getVolunteerId, volunteer.getId()));
        
        if (signup == null) {
            // 自动报名并签到
            signup = new ActivitySignup();
            signup.setActivityId(activity.getId());
            signup.setVolunteerId(volunteer.getId());
            signup.setCreatedAt(LocalDateTime.now());
            signup.setStatus("checked_in");
            signup.setCheckinTime(LocalDateTime.now());
            activitySignupMapper.insert(signup);
        } else if ("checked_in".equals(signup.getStatus())) {
            return ApiResponse.fail("您已签到，无需重复签到");
        } else {
            signup.setStatus("checked_in");
            signup.setCheckinTime(LocalDateTime.now());
            activitySignupMapper.updateById(signup);
        }
        return ApiResponse.ok(signup);
    }

    @GetMapping("/content/{id}")
    public ApiResponse<ContentItem> contentDetail(@PathVariable Long id) {
        return ApiResponse.ok(contentService.findById(id));
    }

    @PostMapping("/heartbeat")
    public ApiResponse<Terminal> heartbeat(@Valid @RequestBody HeartbeatRequest request) {
        return ApiResponse.ok(terminalService.heartbeat(request));
    }

    @GetMapping("/broadcasts/active")
    public ApiResponse<List<BroadcastPlaybackDto>> activeBroadcasts(@RequestParam String terminalCode) {
        Terminal terminal = terminalMapper.selectOne(new LambdaQueryWrapper<Terminal>().eq(Terminal::getCode, terminalCode));
        String groupName = terminal != null ? terminal.getGroupName() : null;
        Page<BroadcastJob> page = broadcastService.activeForTerminal(terminalCode, groupName, 1, 50);
        List<BroadcastPlaybackDto> list = page.getRecords().stream().map(job -> {
            BroadcastPlaybackDto dto = new BroadcastPlaybackDto();
            dto.setJob(job);
            if (job.getMediaId() != null) {
                MediaAsset media = mediaAssetMapper.selectById(job.getMediaId());
                dto.setMedia(media);
            }
            if (job.getContentId() != null) {
                ContentItem content = contentItemMapper.selectById(job.getContentId());
                dto.setContent(content);
            }
            return dto;
        }).collect(Collectors.toList());
        return ApiResponse.ok(list);
    }

    @GetMapping("/volunteer/signups")
    public ApiResponse<List<VolunteerSignupDto>> volunteerSignups(@RequestParam String phone) {
        Volunteer volunteer = volunteerMapper.selectOne(new LambdaQueryWrapper<Volunteer>().eq(Volunteer::getPhone, phone));
        if (volunteer == null) {
            return ApiResponse.ok(List.of());
        }
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
        }).collect(java.util.stream.Collectors.toList());
        return ApiResponse.ok(list);
    }

    private void logStatus(Long volunteerId, String status, String remark) {
        VolunteerStatusLog log = new VolunteerStatusLog();
        log.setVolunteerId(volunteerId);
        log.setStatus(status);
        log.setRemark(remark);
        log.setCreatedAt(LocalDateTime.now());
        volunteerStatusLogMapper.insert(log);
    }

    private int resolveRecommendLimit(Integer limit) {
        if (limit != null && limit > 0) {
            return limit;
        }
        ContentConfig config = contentConfigMapper.selectOne(null);
        if (config != null && config.getRecommendCount() != null && config.getRecommendCount() > 0) {
            return config.getRecommendCount();
        }
        return 6;
    }

    private List<ContentItem> limitList(List<ContentItem> list, int limit) {
        if (limit <= 0 || list.size() <= limit) {
            return list;
        }
        return list.subList(0, limit);
    }
}

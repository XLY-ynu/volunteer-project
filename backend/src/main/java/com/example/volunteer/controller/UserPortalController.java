/**
 * @Author: 谢龙洋
 * @Module: 加入组织 + 成为志愿者
 * @Description: 普通用户端控制器，支持用户登录、加入志愿者组织、申请成为志愿者
 */
package com.example.volunteer.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.volunteer.common.ApiResponse;
import com.example.volunteer.dto.LoginResponse;
import com.example.volunteer.entity.*;
import com.example.volunteer.mapper.*;
import com.example.volunteer.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 普通用户端控制器
 */
@RestController
@RequestMapping("/api/user-portal")
public class UserPortalController {

    private final UserMapper userMapper;
    private final VolunteerOrgMapper orgMapper;
    private final HelpRequestMapper helpRequestMapper;
    private final VolunteerMapper volunteerMapper;
    private final VolunteerOrgMemberMapper memberMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public UserPortalController(UserMapper userMapper, VolunteerOrgMapper orgMapper,
                                HelpRequestMapper helpRequestMapper, VolunteerMapper volunteerMapper,
                                VolunteerOrgMemberMapper memberMapper, PasswordEncoder passwordEncoder,
                                JwtUtil jwtUtil) {
        this.userMapper = userMapper;
        this.orgMapper = orgMapper;
        this.helpRequestMapper = helpRequestMapper;
        this.volunteerMapper = volunteerMapper;
        this.memberMapper = memberMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        
        if (username == null || password == null) {
            return ApiResponse.fail("用户名和密码不能为空");
        }
        
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username));
        if (user == null) {
            return ApiResponse.fail("用户不存在");
        }
        if (!passwordEncoder.matches(password, user.getPassword())) {
            return ApiResponse.fail("密码错误");
        }
        if (!Boolean.TRUE.equals(user.getEnabled())) {
            return ApiResponse.fail("账号已禁用");
        }
        
        String token = jwtUtil.generateToken(user.getUsername(), user.getRoleCode());
        return ApiResponse.ok(new LoginResponse(token, user.getUsername(), user.getRoleCode()));
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public ApiResponse<Void> register(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        String nickname = body.get("nickname");
        
        if (username == null || password == null) {
            return ApiResponse.fail("用户名和密码不能为空");
        }
        
        // 检查用户名是否已存在
        User existing = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username));
        if (existing != null) {
            return ApiResponse.fail("用户名已存在");
        }
        
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setNickname(nickname != null ? nickname : username);
        user.setRoleCode("USER");
        user.setEnabled(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.insert(user);
        
        return ApiResponse.ok(null);
    }

    /**
     * 获取所有志愿者组织列表
     */
    @GetMapping("/orgs")
    public ApiResponse<List<VolunteerOrg>> getOrgs() {
        List<VolunteerOrg> orgs = orgMapper.selectList(
                new LambdaQueryWrapper<VolunteerOrg>()
                        .eq(VolunteerOrg::getStatus, "active")
                        .orderByDesc(VolunteerOrg::getCreatedAt));
        return ApiResponse.ok(orgs);
    }

    /**
     * 获取组织详情
     */
    @GetMapping("/orgs/{id}")
    public ApiResponse<VolunteerOrg> getOrgDetail(@PathVariable Long id) {
        VolunteerOrg org = orgMapper.selectById(id);
        return ApiResponse.ok(org);
    }

    // ========== 求助功能 ==========

    /**
     * 发布求助
     */
    @PostMapping("/help-requests")
    public ApiResponse<Void> createHelpRequest(@RequestHeader("Authorization") String token,
                                               @RequestBody Map<String, Object> body) {
        Long userId = getUserId(token);
        if (userId == null) return ApiResponse.fail("请先登录");
        
        HelpRequest request = new HelpRequest();
        request.setUserId(userId);
        request.setOrgId(body.get("orgId") != null ? Long.valueOf(body.get("orgId").toString()) : null);
        request.setTitle((String) body.get("title"));
        request.setContent((String) body.get("content"));
        request.setContactName((String) body.get("contactName"));
        request.setContactPhone((String) body.get("contactPhone"));
        request.setAddress((String) body.get("address"));
        request.setStatus("pending");
        request.setCreatedAt(LocalDateTime.now());
        request.setUpdatedAt(LocalDateTime.now());
        
        helpRequestMapper.insert(request);
        return ApiResponse.ok(null);
    }

    /**
     * 获取我的求助列表
     */
    @GetMapping("/help-requests")
    public ApiResponse<Page<Map<String, Object>>> getMyHelpRequests(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = getUserId(token);
        if (userId == null) return ApiResponse.fail("请先登录");
        
        Page<HelpRequest> requestPage = helpRequestMapper.selectPage(
                new Page<>(page, size),
                new LambdaQueryWrapper<HelpRequest>()
                        .eq(HelpRequest::getUserId, userId)
                        .orderByDesc(HelpRequest::getCreatedAt));
        
        // 转换为包含组织信息的结果
        Page<Map<String, Object>> result = new Page<>(page, size, requestPage.getTotal());
        List<Map<String, Object>> records = requestPage.getRecords().stream().map(r -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", r.getId());
            map.put("title", r.getTitle());
            map.put("content", r.getContent());
            map.put("status", r.getStatus());
            map.put("reply", r.getReply());
            map.put("repliedAt", r.getRepliedAt());
            map.put("createdAt", r.getCreatedAt());
            if (r.getOrgId() != null) {
                VolunteerOrg org = orgMapper.selectById(r.getOrgId());
                if (org != null) {
                    map.put("orgName", org.getName());
                }
            }
            return map;
        }).toList();
        result.setRecords(records);
        
        return ApiResponse.ok(result);
    }

    // ========== 成为志愿者 ==========

    /**
     * 获取当前用户的志愿者状态
     */
    @GetMapping("/volunteer-status")
    public ApiResponse<Map<String, Object>> getVolunteerStatus(@RequestHeader("Authorization") String token) {
        Long userId = getUserId(token);
        if (userId == null) return ApiResponse.fail("请先登录");
        
        Volunteer volunteer = volunteerMapper.selectOne(
                new LambdaQueryWrapper<Volunteer>().eq(Volunteer::getUserId, userId));
        
        if (volunteer == null) {
            return ApiResponse.ok(null);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("id", volunteer.getId());
        result.put("name", volunteer.getName());
        result.put("phone", volunteer.getPhone());
        result.put("status", volunteer.getStatus());
        result.put("createdAt", volunteer.getCreatedAt());
        return ApiResponse.ok(result);
    }

    /**
     * 申请成为志愿者
     */
    @PostMapping("/become-volunteer")
    public ApiResponse<Void> becomeVolunteer(@RequestHeader("Authorization") String token,
                                             @RequestBody Map<String, String> body) {
        Long userId = getUserId(token);
        if (userId == null) return ApiResponse.fail("请先登录");
        
        User user = userMapper.selectById(userId);
        if (user == null) return ApiResponse.fail("用户不存在");
        
        // 检查是否已经有志愿者记录
        Volunteer existing = volunteerMapper.selectOne(
                new LambdaQueryWrapper<Volunteer>().eq(Volunteer::getUserId, userId));
        
        if (existing != null) {
            // 如果已经是审核通过的志愿者
            if ("approved".equals(existing.getStatus())) {
                return ApiResponse.fail("您已经是志愿者了");
            }
            // 如果正在审核中
            if ("pending".equals(existing.getStatus())) {
                return ApiResponse.fail("您的申请正在审核中，请耐心等待");
            }
            // 如果之前被拒绝，允许重新申请（更新原记录）
            if ("rejected".equals(existing.getStatus())) {
                existing.setName(body.getOrDefault("name", user.getNickname()));
                existing.setPhone(body.get("phone"));
                existing.setEmail(body.get("email"));
                existing.setOrganization(body.get("organization"));
                existing.setStatus("pending");
                existing.setUpdatedAt(LocalDateTime.now());
                volunteerMapper.updateById(existing);
                return ApiResponse.ok(null);
            }
        }
        
        // 创建新的志愿者记录
        Volunteer volunteer = new Volunteer();
        volunteer.setUserId(userId);
        volunteer.setName(body.getOrDefault("name", user.getNickname()));
        volunteer.setPhone(body.get("phone"));
        volunteer.setEmail(body.get("email"));
        volunteer.setOrganization(body.get("organization"));
        volunteer.setStatus("pending");
        volunteer.setCreatedAt(LocalDateTime.now());
        volunteer.setUpdatedAt(LocalDateTime.now());
        volunteerMapper.insert(volunteer);
        
        return ApiResponse.ok(null);
    }

    /**
     * 申请加入志愿者组织
     */
    @PostMapping("/join-org/{orgId}")
    public ApiResponse<Void> joinOrg(@RequestHeader("Authorization") String token,
                                     @PathVariable Long orgId) {
        Long userId = getUserId(token);
        if (userId == null) return ApiResponse.fail("请先登录");
        
        // 检查是否是志愿者
        Volunteer volunteer = volunteerMapper.selectOne(
                new LambdaQueryWrapper<Volunteer>()
                        .eq(Volunteer::getUserId, userId)
                        .eq(Volunteer::getStatus, "approved"));
        if (volunteer == null) {
            return ApiResponse.fail("请先申请成为志愿者并等待审核通过");
        }
        
        // 检查组织是否存在
        VolunteerOrg org = orgMapper.selectById(orgId);
        if (org == null) return ApiResponse.fail("组织不存在");
        
        // 检查是否已申请
        VolunteerOrgMember existing = memberMapper.selectOne(
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
        memberMapper.insert(member);
        
        return ApiResponse.ok(null);
    }

    /**
     * 获取我加入的组织
     */
    @GetMapping("/my-orgs")
    public ApiResponse<List<Map<String, Object>>> getMyOrgs(@RequestHeader("Authorization") String token) {
        Long userId = getUserId(token);
        if (userId == null) return ApiResponse.fail("请先登录");
        
        Volunteer volunteer = volunteerMapper.selectOne(
                new LambdaQueryWrapper<Volunteer>().eq(Volunteer::getUserId, userId));
        if (volunteer == null) {
            return ApiResponse.ok(List.of());
        }
        
        List<VolunteerOrgMember> members = memberMapper.selectList(
                new LambdaQueryWrapper<VolunteerOrgMember>()
                        .eq(VolunteerOrgMember::getVolunteerId, volunteer.getId()));
        
        List<Map<String, Object>> result = members.stream().map(m -> {
            Map<String, Object> map = new HashMap<>();
            map.put("memberId", m.getId());
            map.put("status", m.getStatus());
            map.put("joinedAt", m.getJoinedAt());
            VolunteerOrg org = orgMapper.selectById(m.getOrgId());
            if (org != null) {
                map.put("orgId", org.getId());
                map.put("orgName", org.getName());
                map.put("orgLogo", org.getLogoUrl());
            }
            return map;
        }).toList();
        
        return ApiResponse.ok(result);
    }

    // ========== 辅助方法 ==========

    private Long getUserId(String token) {
        String username = extractUsername(token);
        if (username == null) return null;
        
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        return user != null ? user.getId() : null;
    }

    private String extractUsername(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        try {
            String[] parts = token.split("\\.");
            if (parts.length >= 2) {
                String payload = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));
                int start = payload.indexOf("\"sub\":\"") + 7;
                int end = payload.indexOf("\"", start);
                return payload.substring(start, end);
            }
        } catch (Exception e) {
            // ignore
        }
        return null;
    }
}

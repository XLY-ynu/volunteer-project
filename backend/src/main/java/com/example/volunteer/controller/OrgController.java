/**
 * @Author: 曹宇涵 + 梁玉杰
 * @Module: 求助处理 + 志愿者管理
 * @Description: 志愿者组织端控制器，曹宇涵负责求助处理，梁玉杰负责志愿者审核
 */
package com.example.volunteer.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.volunteer.common.ApiResponse;
import com.example.volunteer.dto.LoginRequest;
import com.example.volunteer.dto.LoginResponse;
import com.example.volunteer.entity.*;
import com.example.volunteer.mapper.*;
import com.example.volunteer.service.impl.AuthServiceImpl;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 志愿者组织端控制器
 */
@RestController
@RequestMapping("/api/org")
public class OrgController {

    private final AuthServiceImpl authService;
    private final VolunteerOrgMapper orgMapper;
    private final VolunteerOrgMemberMapper memberMapper;
    private final VolunteerMapper volunteerMapper;
    private final HelpRequestMapper helpRequestMapper;
    private final ActivityMapper activityMapper;
    private final ActivitySignupMapper signupMapper;
    private final UserMapper userMapper;

    public OrgController(AuthServiceImpl authService, VolunteerOrgMapper orgMapper,
                         VolunteerOrgMemberMapper memberMapper, VolunteerMapper volunteerMapper,
                         HelpRequestMapper helpRequestMapper, ActivityMapper activityMapper,
                         ActivitySignupMapper signupMapper, UserMapper userMapper) {
        this.authService = authService;
        this.orgMapper = orgMapper;
        this.memberMapper = memberMapper;
        this.volunteerMapper = volunteerMapper;
        this.helpRequestMapper = helpRequestMapper;
        this.activityMapper = activityMapper;
        this.signupMapper = signupMapper;
        this.userMapper = userMapper;
    }

    /**
     * 组织端登录
     */
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@RequestBody LoginRequest request) {
        return ApiResponse.ok(authService.orgLogin(request));
    }

    /**
     * 获取组织信息
     */
    @GetMapping("/info")
    public ApiResponse<VolunteerOrg> getOrgInfo(@RequestHeader("Authorization") String token) {
        String username = extractUsername(token);
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (user == null) return ApiResponse.fail("用户不存在");
        
        VolunteerOrg org = orgMapper.selectOne(new LambdaQueryWrapper<VolunteerOrg>()
                .eq(VolunteerOrg::getUserId, user.getId()));
        return ApiResponse.ok(org);
    }

    /**
     * 更新组织信息
     */
    @PutMapping("/info")
    public ApiResponse<Void> updateOrgInfo(@RequestHeader("Authorization") String token,
                                           @RequestBody VolunteerOrg orgInfo) {
        String username = extractUsername(token);
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (user == null) return ApiResponse.fail("用户不存在");
        
        VolunteerOrg org = orgMapper.selectOne(new LambdaQueryWrapper<VolunteerOrg>()
                .eq(VolunteerOrg::getUserId, user.getId()));
        if (org == null) return ApiResponse.fail("组织不存在");
        
        org.setName(orgInfo.getName());
        org.setDescription(orgInfo.getDescription());
        org.setContactName(orgInfo.getContactName());
        org.setContactPhone(orgInfo.getContactPhone());
        org.setContactEmail(orgInfo.getContactEmail());
        org.setAddress(orgInfo.getAddress());
        org.setLogoUrl(orgInfo.getLogoUrl());
        org.setUpdatedAt(LocalDateTime.now());
        orgMapper.updateById(org);
        return ApiResponse.ok(null);
    }

    // ========== 志愿者管理 ==========

    /**
     * 获取申请加入组织的志愿者列表
     */
    @GetMapping("/volunteers/pending")
    public ApiResponse<List<Map<String, Object>>> getPendingVolunteers(@RequestHeader("Authorization") String token) {
        Long orgId = getOrgId(token);
        if (orgId == null) return ApiResponse.fail("组织不存在");
        
        List<VolunteerOrgMember> members = memberMapper.selectList(
                new LambdaQueryWrapper<VolunteerOrgMember>()
                        .eq(VolunteerOrgMember::getOrgId, orgId)
                        .eq(VolunteerOrgMember::getStatus, "pending"));
        
        List<Map<String, Object>> result = members.stream().map(m -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", m.getId());
            map.put("volunteerId", m.getVolunteerId());
            map.put("status", m.getStatus());
            map.put("createdAt", m.getCreatedAt());
            Volunteer v = volunteerMapper.selectById(m.getVolunteerId());
            if (v != null) {
                map.put("name", v.getName());
                map.put("phone", v.getPhone());
                map.put("email", v.getEmail());
            }
            return map;
        }).toList();
        
        return ApiResponse.ok(result);
    }

    /**
     * 审核志愿者申请
     */
    @PostMapping("/volunteers/{memberId}/audit")
    public ApiResponse<Void> auditVolunteer(@RequestHeader("Authorization") String token,
                                            @PathVariable Long memberId,
                                            @RequestParam String action) {
        Long orgId = getOrgId(token);
        if (orgId == null) return ApiResponse.fail("组织不存在");
        
        VolunteerOrgMember member = memberMapper.selectById(memberId);
        if (member == null || !member.getOrgId().equals(orgId)) {
            return ApiResponse.fail("申请记录不存在");
        }
        
        if ("approve".equals(action)) {
            member.setStatus("approved");
            member.setJoinedAt(LocalDateTime.now());
        } else if ("reject".equals(action)) {
            member.setStatus("rejected");
        } else {
            return ApiResponse.fail("无效的操作");
        }
        memberMapper.updateById(member);
        return ApiResponse.ok(null);
    }

    /**
     * 获取组织的志愿者列表
     */
    @GetMapping("/volunteers")
    public ApiResponse<List<Map<String, Object>>> getOrgVolunteers(@RequestHeader("Authorization") String token) {
        Long orgId = getOrgId(token);
        if (orgId == null) return ApiResponse.fail("组织不存在");
        
        List<VolunteerOrgMember> members = memberMapper.selectList(
                new LambdaQueryWrapper<VolunteerOrgMember>()
                        .eq(VolunteerOrgMember::getOrgId, orgId)
                        .eq(VolunteerOrgMember::getStatus, "approved"));
        
        List<Map<String, Object>> result = members.stream().map(m -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", m.getId());
            map.put("volunteerId", m.getVolunteerId());
            map.put("joinedAt", m.getJoinedAt());
            Volunteer v = volunteerMapper.selectById(m.getVolunteerId());
            if (v != null) {
                map.put("name", v.getName());
                map.put("phone", v.getPhone());
                map.put("email", v.getEmail());
            }
            return map;
        }).toList();
        
        return ApiResponse.ok(result);
    }

    // ========== 求助管理 ==========

    /**
     * 获取收到的求助列表
     */
    @GetMapping("/help-requests")
    public ApiResponse<Page<HelpRequest>> getHelpRequests(@RequestHeader("Authorization") String token,
                                                          @RequestParam(defaultValue = "1") int page,
                                                          @RequestParam(defaultValue = "10") int size,
                                                          @RequestParam(required = false) String status) {
        Long orgId = getOrgId(token);
        if (orgId == null) return ApiResponse.fail("组织不存在");
        
        LambdaQueryWrapper<HelpRequest> wrapper = new LambdaQueryWrapper<HelpRequest>()
                .eq(HelpRequest::getOrgId, orgId)
                .orderByDesc(HelpRequest::getCreatedAt);
        if (status != null && !status.isEmpty()) {
            wrapper.eq(HelpRequest::getStatus, status);
        }
        
        Page<HelpRequest> result = helpRequestMapper.selectPage(new Page<>(page, size), wrapper);
        return ApiResponse.ok(result);
    }

    /**
     * 处理求助
     */
    @PostMapping("/help-requests/{id}/reply")
    public ApiResponse<Void> replyHelpRequest(@RequestHeader("Authorization") String token,
                                              @PathVariable Long id,
                                              @RequestBody Map<String, String> body) {
        Long orgId = getOrgId(token);
        if (orgId == null) return ApiResponse.fail("组织不存在");
        
        HelpRequest request = helpRequestMapper.selectById(id);
        if (request == null || !request.getOrgId().equals(orgId)) {
            return ApiResponse.fail("求助记录不存在");
        }
        
        String reply = body.get("reply");
        String status = body.getOrDefault("status", "completed");
        
        request.setReply(reply);
        request.setStatus(status);
        request.setRepliedAt(LocalDateTime.now());
        request.setUpdatedAt(LocalDateTime.now());
        helpRequestMapper.updateById(request);
        
        return ApiResponse.ok(null);
    }

    // ========== 统计 ==========

    /**
     * 获取组织统计数据
     */
    @GetMapping("/stats")
    public ApiResponse<Map<String, Object>> getStats(@RequestHeader("Authorization") String token) {
        Long orgId = getOrgId(token);
        if (orgId == null) return ApiResponse.fail("组织不存在");
        
        Map<String, Object> stats = new HashMap<>();
        
        // 志愿者数量
        long volunteerCount = memberMapper.selectCount(
                new LambdaQueryWrapper<VolunteerOrgMember>()
                        .eq(VolunteerOrgMember::getOrgId, orgId)
                        .eq(VolunteerOrgMember::getStatus, "approved"));
        stats.put("volunteerCount", volunteerCount);
        
        // 待审核数量
        long pendingCount = memberMapper.selectCount(
                new LambdaQueryWrapper<VolunteerOrgMember>()
                        .eq(VolunteerOrgMember::getOrgId, orgId)
                        .eq(VolunteerOrgMember::getStatus, "pending"));
        stats.put("pendingCount", pendingCount);
        
        // 待处理求助数量
        long helpPendingCount = helpRequestMapper.selectCount(
                new LambdaQueryWrapper<HelpRequest>()
                        .eq(HelpRequest::getOrgId, orgId)
                        .eq(HelpRequest::getStatus, "pending"));
        stats.put("helpPendingCount", helpPendingCount);
        
        return ApiResponse.ok(stats);
    }

    // ========== 辅助方法 ==========

    private String extractUsername(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        // 简单解析JWT获取用户名（实际应使用JwtUtil）
        try {
            String[] parts = token.split("\\.");
            if (parts.length >= 2) {
                String payload = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));
                // 简单解析JSON
                int start = payload.indexOf("\"sub\":\"") + 7;
                int end = payload.indexOf("\"", start);
                return payload.substring(start, end);
            }
        } catch (Exception e) {
            // ignore
        }
        return null;
    }

    private Long getOrgId(String token) {
        String username = extractUsername(token);
        if (username == null) return null;
        
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (user == null) return null;
        
        VolunteerOrg org = orgMapper.selectOne(new LambdaQueryWrapper<VolunteerOrg>()
                .eq(VolunteerOrg::getUserId, user.getId()));
        return org != null ? org.getId() : null;
    }
}

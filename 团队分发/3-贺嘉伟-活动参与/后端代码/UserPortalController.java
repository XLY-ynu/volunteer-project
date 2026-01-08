/**
 * @Author: 贺嘉伟
 * @Module: 加入志愿者组织 + 成为志愿者
 * @Description: 普通用户端控制器，支持用户加入志愿者组织、申请成为志愿者
 */
package com.example.volunteer.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.volunteer.common.ApiResponse;
import com.example.volunteer.entity.*;
import com.example.volunteer.mapper.*;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 普通用户端控制器 - 加入组织与成为志愿者功能
 */
@RestController
@RequestMapping("/api/user-portal")
public class UserPortalController {

    private final UserMapper userMapper;
    private final VolunteerOrgMapper orgMapper;
    private final VolunteerMapper volunteerMapper;
    private final VolunteerOrgMemberMapper memberMapper;

    public UserPortalController(UserMapper userMapper, VolunteerOrgMapper orgMapper,
                                VolunteerMapper volunteerMapper, VolunteerOrgMemberMapper memberMapper) {
        this.userMapper = userMapper;
        this.orgMapper = orgMapper;
        this.volunteerMapper = volunteerMapper;
        this.memberMapper = memberMapper;
    }

    // ========== 组织列表功能 ==========

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
        
        // 检查是否已经是志愿者
        Volunteer existing = volunteerMapper.selectOne(
                new LambdaQueryWrapper<Volunteer>().eq(Volunteer::getUserId, userId));
        if (existing != null) {
            return ApiResponse.fail("您已经是志愿者了");
        }
        
        // 创建志愿者记录
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

    // ========== 加入志愿者组织 ==========

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

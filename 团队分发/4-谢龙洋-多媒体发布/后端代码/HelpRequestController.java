/**
 * @Author: 谢龙洋
 * @Module: 普通用户端模块三 - 发布求助
 * @Description: 普通用户发布求助功能
 *   - 选择组织：从组织列表中选择要求助的志愿者组织
 *   - 填写求助：填写求助标题、详细内容、联系人、联系电话、地址
 *   - 提交求助：向指定组织发送求助请求
 *   - 我的求助：查看已提交的求助记录及处理状态
 */
package com.example.volunteer.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.volunteer.common.ApiResponse;
import com.example.volunteer.entity.*;
import com.example.volunteer.mapper.*;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 普通用户发布求助控制器
 */
@RestController
@RequestMapping("/api/user-portal")
public class HelpRequestController {

    private final UserMapper userMapper;
    private final VolunteerOrgMapper orgMapper;
    private final HelpRequestMapper helpRequestMapper;

    public HelpRequestController(UserMapper userMapper, VolunteerOrgMapper orgMapper,
                                 HelpRequestMapper helpRequestMapper) {
        this.userMapper = userMapper;
        this.orgMapper = orgMapper;
        this.helpRequestMapper = helpRequestMapper;
    }

    // ========== 选择组织 ==========

    /**
     * 获取所有志愿者组织列表（用于选择求助对象）
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

    // ========== 填写求助 & 提交求助 ==========

    /**
     * 发布求助
     * 填写求助标题、详细内容、联系人、联系电话、地址
     * 向指定组织发送求助请求
     */
    @PostMapping("/help-requests")
    public ApiResponse<Void> createHelpRequest(@RequestHeader("Authorization") String token,
                                               @RequestBody Map<String, Object> body) {
        Long userId = getUserId(token);
        if (userId == null) return ApiResponse.fail("请先登录");
        
        // 验证必填字段
        if (body.get("orgId") == null) {
            return ApiResponse.fail("请选择求助的组织");
        }
        if (body.get("title") == null || body.get("title").toString().trim().isEmpty()) {
            return ApiResponse.fail("请填写求助标题");
        }
        if (body.get("content") == null || body.get("content").toString().trim().isEmpty()) {
            return ApiResponse.fail("请填写详细内容");
        }
        
        // 验证组织是否存在
        Long orgId = Long.valueOf(body.get("orgId").toString());
        VolunteerOrg org = orgMapper.selectById(orgId);
        if (org == null) {
            return ApiResponse.fail("所选组织不存在");
        }
        
        // 创建求助记录
        HelpRequest request = new HelpRequest();
        request.setUserId(userId);
        request.setOrgId(orgId);
        request.setTitle((String) body.get("title"));
        request.setContent((String) body.get("content"));
        request.setContactName((String) body.get("contactName"));
        request.setContactPhone((String) body.get("contactPhone"));
        request.setAddress((String) body.get("address"));
        request.setStatus("pending"); // 待处理
        request.setCreatedAt(LocalDateTime.now());
        request.setUpdatedAt(LocalDateTime.now());
        
        helpRequestMapper.insert(request);
        return ApiResponse.ok(null);
    }

    // ========== 我的求助 ==========

    /**
     * 获取我的求助列表
     * 查看已提交的求助记录及处理状态（待处理/处理中/已完成）
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
            map.put("contactName", r.getContactName());
            map.put("contactPhone", r.getContactPhone());
            map.put("address", r.getAddress());
            map.put("status", r.getStatus());
            map.put("reply", r.getReply());
            map.put("repliedAt", r.getRepliedAt());
            map.put("createdAt", r.getCreatedAt());
            // 获取组织名称
            if (r.getOrgId() != null) {
                VolunteerOrg org = orgMapper.selectById(r.getOrgId());
                if (org != null) {
                    map.put("orgId", org.getId());
                    map.put("orgName", org.getName());
                }
            }
            return map;
        }).toList();
        result.setRecords(records);
        
        return ApiResponse.ok(result);
    }

    /**
     * 获取单个求助详情
     */
    @GetMapping("/help-requests/{id}")
    public ApiResponse<Map<String, Object>> getHelpRequestDetail(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id) {
        Long userId = getUserId(token);
        if (userId == null) return ApiResponse.fail("请先登录");
        
        HelpRequest request = helpRequestMapper.selectById(id);
        if (request == null) {
            return ApiResponse.fail("求助记录不存在");
        }
        if (!request.getUserId().equals(userId)) {
            return ApiResponse.fail("无权查看此记录");
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("id", request.getId());
        result.put("title", request.getTitle());
        result.put("content", request.getContent());
        result.put("contactName", request.getContactName());
        result.put("contactPhone", request.getContactPhone());
        result.put("address", request.getAddress());
        result.put("status", request.getStatus());
        result.put("reply", request.getReply());
        result.put("repliedAt", request.getRepliedAt());
        result.put("createdAt", request.getCreatedAt());
        
        if (request.getOrgId() != null) {
            VolunteerOrg org = orgMapper.selectById(request.getOrgId());
            if (org != null) {
                result.put("orgId", org.getId());
                result.put("orgName", org.getName());
            }
        }
        
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

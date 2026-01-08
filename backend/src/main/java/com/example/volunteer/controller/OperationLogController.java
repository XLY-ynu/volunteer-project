/**
 * @Author: 陈力宏
 * @Module: 系统管理 - 操作日志
 * @Description: 操作日志控制器，支持日志查询、筛选、清理
 */
package com.example.volunteer.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.volunteer.common.ApiResponse;
import com.example.volunteer.entity.OperationLog;
import com.example.volunteer.mapper.OperationLogMapper;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/ops")
public class OperationLogController {

    private final OperationLogMapper operationLogMapper;

    public OperationLogController(OperationLogMapper operationLogMapper) {
        this.operationLogMapper = operationLogMapper;
    }

    @GetMapping("/logs")
    public ApiResponse<Page<OperationLog>> logs(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String method,
            @RequestParam(required = false) String path,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();
        
        // 筛选条件
        if (username != null && !username.isEmpty()) {
            wrapper.like(OperationLog::getUsername, username);
        }
        if (method != null && !method.isEmpty()) {
            wrapper.eq(OperationLog::getMethod, method);
        }
        if (path != null && !path.isEmpty()) {
            wrapper.like(OperationLog::getPath, path);
        }
        if (startDate != null && !startDate.isEmpty()) {
            LocalDateTime start = LocalDate.parse(startDate).atStartOfDay();
            wrapper.ge(OperationLog::getCreatedAt, start);
        }
        if (endDate != null && !endDate.isEmpty()) {
            LocalDateTime end = LocalDate.parse(endDate).atTime(LocalTime.MAX);
            wrapper.le(OperationLog::getCreatedAt, end);
        }
        
        // 按时间倒序
        wrapper.orderByDesc(OperationLog::getCreatedAt);
        
        Page<OperationLog> p = new Page<>(page, size);
        operationLogMapper.selectPage(p, wrapper);
        return ApiResponse.ok(p);
    }

    @DeleteMapping("/logs/clean")
    public ApiResponse<Map<String, Long>> cleanLogs(@RequestParam(defaultValue = "30") int days) {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(days);
        
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.lt(OperationLog::getCreatedAt, cutoff);
        
        long deleted = operationLogMapper.delete(wrapper);
        
        Map<String, Long> result = new HashMap<>();
        result.put("deleted", deleted);
        return ApiResponse.ok(result);
    }
}

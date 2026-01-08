/**
 * @Author: 谢龙洋
 * @Module: 多媒体发布 - 广播推送
 * @Description: 广播控制器，负责内容下发和广播任务管理
 */
package com.example.volunteer.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.volunteer.common.ApiResponse;
import com.example.volunteer.dto.BroadcastRequest;
import com.example.volunteer.entity.BroadcastJob;
import com.example.volunteer.service.BroadcastService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/broadcasts")
public class BroadcastController {

    private final BroadcastService broadcastService;

    public BroadcastController(BroadcastService broadcastService) {
        this.broadcastService = broadcastService;
    }

    @PostMapping
    public ApiResponse<BroadcastJob> create(@Valid @RequestBody BroadcastRequest request) {
        return ApiResponse.ok(broadcastService.create(request));
    }

    @GetMapping
    public ApiResponse<Page<BroadcastJob>> page(@RequestParam(defaultValue = "1") int page,
                                                @RequestParam(defaultValue = "10") int size,
                                                @RequestParam(required = false) String targetGroup,
                                                @RequestParam(required = false) String targetTerminalCode) {
        return ApiResponse.ok(broadcastService.page(page, size, targetGroup, targetTerminalCode));
    }

    @GetMapping("/active")
    public ApiResponse<Page<BroadcastJob>> active(@RequestParam String terminalCode,
                                                  @RequestParam(required = false) String groupName,
                                                  @RequestParam(defaultValue = "1") int page,
                                                  @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.ok(broadcastService.activeForTerminal(terminalCode, groupName, page, size));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        broadcastService.delete(id);
        return ApiResponse.ok(null);
    }

    @GetMapping("/status-count")
    public ApiResponse<java.util.Map<String, Long>> statusCount() {
        return ApiResponse.ok(broadcastService.countByStatus());
    }
}

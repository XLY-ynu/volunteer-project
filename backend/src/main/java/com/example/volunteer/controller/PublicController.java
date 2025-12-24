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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/public")
public class PublicController {

    private final MenuCategoryMapper menuCategoryMapper;
    private final ContentService contentService;
    private final TerminalService terminalService;

    public PublicController(MenuCategoryMapper menuCategoryMapper, ContentService contentService, TerminalService terminalService) {
        this.menuCategoryMapper = menuCategoryMapper;
        this.contentService = contentService;
        this.terminalService = terminalService;
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
}

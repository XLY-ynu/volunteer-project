package com.example.volunteer.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.volunteer.common.ApiResponse;
import com.example.volunteer.dto.ContentItemRequest;
import com.example.volunteer.dto.ContentFlagsRequest;
import com.example.volunteer.dto.ContentOrderRequest;
import com.example.volunteer.dto.ContentConfigRequest;
import com.example.volunteer.entity.ContentConfig;
import com.example.volunteer.mapper.ContentConfigMapper;
import com.example.volunteer.entity.ContentItem;
import com.example.volunteer.service.ContentService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/content")
public class ContentController {

    private final ContentService contentService;
    private final ContentConfigMapper contentConfigMapper;

    public ContentController(ContentService contentService, ContentConfigMapper contentConfigMapper) {
        this.contentService = contentService;
        this.contentConfigMapper = contentConfigMapper;
    }

    @PostMapping
    public ApiResponse<ContentItem> create(@Valid @RequestBody ContentItemRequest request) {
        return ApiResponse.ok(contentService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<ContentItem> update(@PathVariable Long id, @Valid @RequestBody ContentItemRequest request) {
        return ApiResponse.ok(contentService.update(id, request));
    }

    @PutMapping("/{id}/flags")
    public ApiResponse<ContentItem> updateFlags(@PathVariable Long id, @RequestBody ContentFlagsRequest request) {
        return ApiResponse.ok(contentService.updateFlags(id, request.getHeadline(), request.getRecommended()));
    }

    @PutMapping("/reorder")
    public ApiResponse<Void> reorder(@RequestBody ContentOrderRequest request) {
        contentService.reorder(request.getItems());
        return ApiResponse.ok(null);
    }

    @GetMapping("/recommended")
    public ApiResponse<java.util.List<ContentItem>> recommended() {
        return ApiResponse.ok(contentService.listRecommended());
    }

    @GetMapping("/headlines")
    public ApiResponse<java.util.List<ContentItem>> headlines() {
        return ApiResponse.ok(contentService.listHeadlines());
    }

    @GetMapping("/config")
    public ApiResponse<ContentConfig> getConfig() {
        ContentConfig config = contentConfigMapper.selectOne(null);
        if (config == null) {
            config = new ContentConfig();
            config.setRecommendIntervalSec(6);
            config.setPreviewIntervalSec(10);
            config.setUpdatedAt(java.time.LocalDateTime.now());
            contentConfigMapper.insert(config);
        }
        return ApiResponse.ok(config);
    }

    @PutMapping("/config")
    public ApiResponse<ContentConfig> updateConfig(@RequestBody ContentConfigRequest request) {
        ContentConfig config = contentConfigMapper.selectOne(null);
        if (config == null) {
            config = new ContentConfig();
        }
        if (request.getRecommendIntervalSec() != null) {
            config.setRecommendIntervalSec(request.getRecommendIntervalSec());
        }
        if (request.getPreviewIntervalSec() != null) {
            config.setPreviewIntervalSec(request.getPreviewIntervalSec());
        }
        config.setUpdatedAt(java.time.LocalDateTime.now());
        if (config.getId() == null) {
            contentConfigMapper.insert(config);
        } else {
            contentConfigMapper.updateById(config);
        }
        return ApiResponse.ok(config);
    }

    @GetMapping
    public ApiResponse<Page<ContentItem>> page(@RequestParam(defaultValue = "1") int page,
                                               @RequestParam(defaultValue = "10") int size,
                                               @RequestParam(required = false) Long categoryId,
                                               @RequestParam(required = false) Boolean published,
                                               @RequestParam(required = false) String keyword) {
        return ApiResponse.ok(contentService.page(page, size, categoryId, published, keyword));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        contentService.delete(id);
        return ApiResponse.ok(null);
    }

    @GetMapping("/{id}")
    public ApiResponse<ContentItem> findById(@PathVariable Long id) {
        return ApiResponse.ok(contentService.findById(id));
    }
}

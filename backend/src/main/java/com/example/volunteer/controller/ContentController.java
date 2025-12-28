package com.example.volunteer.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.volunteer.common.ApiResponse;
import com.example.volunteer.dto.ContentItemRequest;
import com.example.volunteer.dto.ContentFlagsRequest;
import com.example.volunteer.dto.ContentOrderRequest;
import com.example.volunteer.entity.ContentItem;
import com.example.volunteer.service.ContentService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/content")
public class ContentController {

    private final ContentService contentService;

    public ContentController(ContentService contentService) {
        this.contentService = contentService;
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

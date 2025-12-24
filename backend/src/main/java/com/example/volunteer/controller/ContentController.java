package com.example.volunteer.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.volunteer.common.ApiResponse;
import com.example.volunteer.dto.ContentItemRequest;
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

    @GetMapping
    public ApiResponse<Page<ContentItem>> page(@RequestParam(defaultValue = "1") int page,
                                               @RequestParam(defaultValue = "10") int size,
                                               @RequestParam(required = false) Long categoryId,
                                               @RequestParam(required = false) Boolean published) {
        return ApiResponse.ok(contentService.page(page, size, categoryId, published));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        contentService.delete(id);
        return ApiResponse.ok(null);
    }
}

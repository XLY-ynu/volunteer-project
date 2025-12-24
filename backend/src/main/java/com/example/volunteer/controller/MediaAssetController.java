package com.example.volunteer.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.volunteer.common.ApiResponse;
import com.example.volunteer.dto.MediaAssetRequest;
import com.example.volunteer.entity.MediaAsset;
import com.example.volunteer.service.MediaAssetService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/media")
public class MediaAssetController {

    private final MediaAssetService mediaAssetService;

    public MediaAssetController(MediaAssetService mediaAssetService) {
        this.mediaAssetService = mediaAssetService;
    }

    @PostMapping
    public ApiResponse<MediaAsset> create(@Valid @RequestBody MediaAssetRequest request) {
        return ApiResponse.ok(mediaAssetService.create(request));
    }

    @GetMapping
    public ApiResponse<Page<MediaAsset>> page(@RequestParam(defaultValue = "1") int page,
                                              @RequestParam(defaultValue = "10") int size,
                                              @RequestParam(required = false) String type) {
        return ApiResponse.ok(mediaAssetService.page(page, size, type));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        mediaAssetService.delete(id);
        return ApiResponse.ok(null);
    }
}

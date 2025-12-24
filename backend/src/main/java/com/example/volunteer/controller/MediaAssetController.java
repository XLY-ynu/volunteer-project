package com.example.volunteer.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.volunteer.common.ApiResponse;
import com.example.volunteer.dto.MediaAssetRequest;
import com.example.volunteer.entity.MediaAsset;
import com.example.volunteer.service.MediaAssetService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

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

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<MediaAsset> upload(@RequestPart("file") MultipartFile file,
                                          @RequestPart(value = "type", required = false) String type) {
        return ApiResponse.ok(mediaAssetService.upload(file, type));
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

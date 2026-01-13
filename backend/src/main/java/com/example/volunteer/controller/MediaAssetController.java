/**
 * @Author: 孔令超
 * @Module: 视频展示管理 - 媒体资源库
 * @Description: 媒体资源控制器，提供视频、图片资源的上传/存储功能
 */
package com.example.volunteer.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.volunteer.common.ApiResponse;
import com.example.volunteer.config.WebConfig;
import com.example.volunteer.dto.MediaAssetRequest;
import com.example.volunteer.entity.MediaAsset;
import com.example.volunteer.service.MediaAssetService;
import jakarta.validation.Valid;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/media")
public class MediaAssetController {

    private final MediaAssetService mediaAssetService;
    private final WebConfig webConfig;

    public MediaAssetController(MediaAssetService mediaAssetService, WebConfig webConfig) {
        this.mediaAssetService = mediaAssetService;
        this.webConfig = webConfig;
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

    /**
     * 上传封面图片（仅保存文件，不创建媒体资源记录）
     * 用于内容封面等附属图片，不会出现在媒体库中
     */
    @PostMapping(value = "/upload-cover", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<java.util.Map<String, String>> uploadCover(@RequestPart("file") MultipartFile file) {
        String url = mediaAssetService.uploadCoverOnly(file);
        return ApiResponse.ok(java.util.Map.of("url", url));
    }

    @PostMapping(value = "/{id}/thumb", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<MediaAsset> uploadThumb(@PathVariable Long id, @RequestPart("file") MultipartFile file) {
        return ApiResponse.ok(mediaAssetService.uploadThumb(id, file));
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<FileSystemResource> download(@PathVariable Long id) throws java.io.IOException {
        MediaAsset asset = mediaAssetService.findById(id);
        if (asset == null || asset.getUrl() == null) {
            return ResponseEntity.notFound().build();
        }
        if (!asset.getUrl().startsWith("/uploads/")) {
            return ResponseEntity.badRequest().build();
        }
        java.nio.file.Path path = webConfig.getResolvedUploadPath()
                .resolve(asset.getUrl().replaceFirst("^/uploads/", ""));
        FileSystemResource resource = new FileSystemResource(path.toFile());
        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + asset.getName() + "\"")
                .contentLength(resource.contentLength())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
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

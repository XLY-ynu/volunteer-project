package com.example.volunteer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.volunteer.dto.MediaAssetRequest;
import com.example.volunteer.entity.MediaAsset;
import com.example.volunteer.mapper.MediaAssetMapper;
import com.example.volunteer.service.MediaAssetService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class MediaAssetServiceImpl implements MediaAssetService {

    private final MediaAssetMapper mediaAssetMapper;
    @Value("${app.storage.root:uploads}")
    private String storageRoot;

    public MediaAssetServiceImpl(MediaAssetMapper mediaAssetMapper) {
        this.mediaAssetMapper = mediaAssetMapper;
    }

    @Override
    public MediaAsset create(MediaAssetRequest request) {
        MediaAsset asset = new MediaAsset();
        asset.setName(request.getName());
        asset.setType(request.getType());
        asset.setUrl(request.getUrl());
        asset.setSizeBytes(request.getSizeBytes());
        asset.setDurationSeconds(request.getDurationSeconds());
        asset.setWidth(request.getWidth());
        asset.setHeight(request.getHeight());
        asset.setChecksum(request.getChecksum());
        asset.setCreatedAt(LocalDateTime.now());
        mediaAssetMapper.insert(asset);
        return asset;
    }

    @Override
    public Page<MediaAsset> page(int page, int size, String type) {
        LambdaQueryWrapper<MediaAsset> wrapper = new LambdaQueryWrapper<>();
        if (type != null && !type.isEmpty()) {
            wrapper.eq(MediaAsset::getType, type);
        }
        Page<MediaAsset> p = new Page<>(page, size);
        mediaAssetMapper.selectPage(p, wrapper);
        return p;
    }

    @Override
    public void delete(Long id) {
        mediaAssetMapper.deleteById(id);
    }

    @Override
    public MediaAsset upload(MultipartFile file, String type) {
        try {
            String original = file.getOriginalFilename();
            String ext = (original != null && original.contains(".")) ? original.substring(original.lastIndexOf('.')) : "";
            String filename = UUID.randomUUID() + ext;
            Path root = Paths.get(storageRoot).toAbsolutePath();
            Files.createDirectories(root);
            Path dest = root.resolve(filename);
            file.transferTo(dest.toFile());

            MediaAsset asset = new MediaAsset();
            asset.setName(original != null ? original : filename);
            asset.setType(type != null ? type : guessType(file.getContentType()));
            asset.setUrl("/uploads/" + filename);
            asset.setSizeBytes(file.getSize());
            asset.setCreatedAt(LocalDateTime.now());
            mediaAssetMapper.insert(asset);
            return asset;
        } catch (Exception e) {
            throw new RuntimeException("上传失败", e);
        }
    }

    private String guessType(String contentType) {
        if (contentType == null) {
            return "unknown";
        }
        if (contentType.startsWith("video")) return "video";
        if (contentType.startsWith("image")) return "image";
        if (contentType.contains("pdf")) return "document";
        return "other";
    }
}

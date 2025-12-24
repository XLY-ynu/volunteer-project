package com.example.volunteer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.volunteer.dto.MediaAssetRequest;
import com.example.volunteer.entity.MediaAsset;
import com.example.volunteer.mapper.MediaAssetMapper;
import com.example.volunteer.service.MediaAssetService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class MediaAssetServiceImpl implements MediaAssetService {

    private final MediaAssetMapper mediaAssetMapper;

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
}

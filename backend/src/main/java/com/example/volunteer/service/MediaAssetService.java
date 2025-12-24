package com.example.volunteer.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.volunteer.dto.MediaAssetRequest;
import com.example.volunteer.entity.MediaAsset;

public interface MediaAssetService {
    MediaAsset create(MediaAssetRequest request);
    Page<MediaAsset> page(int page, int size, String type);
    void delete(Long id);
}

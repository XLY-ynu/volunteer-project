package com.example.volunteer.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.volunteer.dto.MediaAssetRequest;
import com.example.volunteer.entity.MediaAsset;
import org.springframework.web.multipart.MultipartFile;

public interface MediaAssetService {
    MediaAsset create(MediaAssetRequest request);
    Page<MediaAsset> page(int page, int size, String type);
    void delete(Long id);
    MediaAsset upload(MultipartFile file, String type);
    MediaAsset findById(Long id);
    MediaAsset uploadThumb(Long id, MultipartFile file);
}

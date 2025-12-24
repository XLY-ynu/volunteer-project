package com.example.volunteer.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.volunteer.dto.ContentItemRequest;
import com.example.volunteer.entity.ContentItem;

public interface ContentService {
    ContentItem create(ContentItemRequest request);
    ContentItem update(Long id, ContentItemRequest request);
    Page<ContentItem> page(int page, int size, Long categoryId, Boolean published);
    void delete(Long id);
}

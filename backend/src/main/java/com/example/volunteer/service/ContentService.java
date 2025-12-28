package com.example.volunteer.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.volunteer.dto.ContentItemRequest;
import com.example.volunteer.entity.ContentItem;

public interface ContentService {
    ContentItem create(ContentItemRequest request);
    ContentItem update(Long id, ContentItemRequest request);
    ContentItem updateFlags(Long id, Boolean headline, Boolean recommended);
    void reorder(java.util.List<com.example.volunteer.dto.ContentOrderItem> items);
    Page<ContentItem> page(int page, int size, Long categoryId, Boolean published, String keyword);
    void delete(Long id);
    ContentItem findById(Long id);
}

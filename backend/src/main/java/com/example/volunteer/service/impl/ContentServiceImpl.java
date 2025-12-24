package com.example.volunteer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.volunteer.dto.ContentItemRequest;
import com.example.volunteer.entity.ContentItem;
import com.example.volunteer.mapper.ContentItemMapper;
import com.example.volunteer.service.ContentService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ContentServiceImpl implements ContentService {

    private final ContentItemMapper contentItemMapper;

    public ContentServiceImpl(ContentItemMapper contentItemMapper) {
        this.contentItemMapper = contentItemMapper;
    }

    @Override
    public ContentItem create(ContentItemRequest request) {
        ContentItem item = new ContentItem();
        item.setCategoryId(request.getCategoryId());
        item.setTitle(request.getTitle());
        item.setSummary(request.getSummary());
        item.setBody(request.getBody());
        item.setCoverUrl(request.getCoverUrl());
        item.setPublished(Boolean.TRUE.equals(request.getPublished()));
        item.setPublishTime(Boolean.TRUE.equals(request.getPublished()) ? LocalDateTime.now() : null);
        item.setCreatedAt(LocalDateTime.now());
        item.setUpdatedAt(LocalDateTime.now());
        contentItemMapper.insert(item);
        return item;
    }

    @Override
    public ContentItem update(Long id, ContentItemRequest request) {
        ContentItem item = contentItemMapper.selectById(id);
        if (item == null) {
            return null;
        }
        item.setCategoryId(request.getCategoryId());
        item.setTitle(request.getTitle());
        item.setSummary(request.getSummary());
        item.setBody(request.getBody());
        item.setCoverUrl(request.getCoverUrl());
        item.setPublished(Boolean.TRUE.equals(request.getPublished()));
        item.setPublishTime(Boolean.TRUE.equals(request.getPublished()) ? LocalDateTime.now() : null);
        item.setUpdatedAt(LocalDateTime.now());
        contentItemMapper.updateById(item);
        return item;
    }

    @Override
    public Page<ContentItem> page(int page, int size, Long categoryId, Boolean published) {
        LambdaQueryWrapper<ContentItem> wrapper = new LambdaQueryWrapper<>();
        if (categoryId != null) {
            wrapper.eq(ContentItem::getCategoryId, categoryId);
        }
        if (published != null) {
            wrapper.eq(ContentItem::getPublished, published);
        }
        wrapper.orderByDesc(ContentItem::getPublishTime);
        Page<ContentItem> p = new Page<>(page, size);
        contentItemMapper.selectPage(p, wrapper);
        return p;
    }

    @Override
    public void delete(Long id) {
        contentItemMapper.deleteById(id);
    }
}

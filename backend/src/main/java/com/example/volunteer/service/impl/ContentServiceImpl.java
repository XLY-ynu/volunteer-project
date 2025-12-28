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
        item.setHeadline(Boolean.TRUE.equals(request.getHeadline()));
        item.setRecommended(Boolean.TRUE.equals(request.getRecommended()));
        Integer order = request.getSortOrder();
        item.setSortOrder(order != null ? order : (int) (System.currentTimeMillis() / 1000));
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
        item.setHeadline(Boolean.TRUE.equals(request.getHeadline()));
        item.setRecommended(Boolean.TRUE.equals(request.getRecommended()));
        if (request.getSortOrder() != null) {
            item.setSortOrder(request.getSortOrder());
        }
        item.setPublishTime(Boolean.TRUE.equals(request.getPublished()) ? LocalDateTime.now() : null);
        item.setUpdatedAt(LocalDateTime.now());
        contentItemMapper.updateById(item);
        return item;
    }

    @Override
    public ContentItem updateFlags(Long id, Boolean headline, Boolean recommended) {
        ContentItem item = contentItemMapper.selectById(id);
        if (item == null) {
            return null;
        }
        if (headline != null) {
            item.setHeadline(headline);
        }
        if (recommended != null) {
            item.setRecommended(recommended);
        }
        item.setUpdatedAt(LocalDateTime.now());
        contentItemMapper.updateById(item);
        return item;
    }

    @Override
    public void reorder(java.util.List<com.example.volunteer.dto.ContentOrderItem> items) {
        if (items == null) {
            return;
        }
        for (com.example.volunteer.dto.ContentOrderItem item : items) {
            ContentItem entity = contentItemMapper.selectById(item.getId());
            if (entity == null) {
                continue;
            }
            entity.setSortOrder(item.getSortOrder());
            entity.setUpdatedAt(LocalDateTime.now());
            contentItemMapper.updateById(entity);
        }
    }

    @Override
    public java.util.List<ContentItem> listRecommended() {
        return contentItemMapper.selectList(
                new LambdaQueryWrapper<ContentItem>()
                        .eq(ContentItem::getRecommended, true)
                        .orderByAsc(ContentItem::getSortOrder)
                        .orderByDesc(ContentItem::getPublishTime)
        );
    }

    @Override
    public java.util.List<ContentItem> listHeadlines() {
        return contentItemMapper.selectList(
                new LambdaQueryWrapper<ContentItem>()
                        .eq(ContentItem::getHeadline, true)
                        .orderByAsc(ContentItem::getSortOrder)
                        .orderByDesc(ContentItem::getPublishTime)
        );
    }

    @Override
    public Page<ContentItem> page(int page, int size, Long categoryId, Boolean published, String keyword) {
        LambdaQueryWrapper<ContentItem> wrapper = new LambdaQueryWrapper<>();
        if (categoryId != null) {
            wrapper.eq(ContentItem::getCategoryId, categoryId);
        }
        if (published != null) {
            wrapper.eq(ContentItem::getPublished, published);
        }
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(ContentItem::getTitle, keyword).or().like(ContentItem::getSummary, keyword));
        }
        wrapper.orderByDesc(ContentItem::getHeadline)
                .orderByDesc(ContentItem::getRecommended)
                .orderByAsc(ContentItem::getSortOrder)
                .orderByDesc(ContentItem::getPublishTime);
        Page<ContentItem> p = new Page<>(page, size);
        contentItemMapper.selectPage(p, wrapper);
        return p;
    }

    @Override
    public void delete(Long id) {
        contentItemMapper.deleteById(id);
    }

    @Override
    public ContentItem findById(Long id) {
        return contentItemMapper.selectById(id);
    }
}

package com.example.volunteer.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.volunteer.common.ApiResponse;
import com.example.volunteer.entity.LayoutTemplate;
import com.example.volunteer.mapper.LayoutTemplateMapper;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/layout-templates")
public class LayoutTemplateController {

    private final LayoutTemplateMapper layoutTemplateMapper;

    public LayoutTemplateController(LayoutTemplateMapper layoutTemplateMapper) {
        this.layoutTemplateMapper = layoutTemplateMapper;
    }

    @GetMapping
    public ApiResponse<List<LayoutTemplate>> list() {
        LambdaQueryWrapper<LayoutTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(LayoutTemplate::getUpdatedAt);
        return ApiResponse.ok(layoutTemplateMapper.selectList(wrapper));
    }

    @PostMapping
    public ApiResponse<LayoutTemplate> create(@Valid @RequestBody LayoutTemplate template) {
        template.setId(null);
        template.setBuiltin(false);
        template.setCreatedAt(LocalDateTime.now());
        template.setUpdatedAt(LocalDateTime.now());
        layoutTemplateMapper.insert(template);
        return ApiResponse.ok(template);
    }

    @PutMapping("/{id}")
    public ApiResponse<LayoutTemplate> update(@PathVariable Long id, @Valid @RequestBody LayoutTemplate template) {
        LayoutTemplate existing = layoutTemplateMapper.selectById(id);
        if (existing == null) {
            return ApiResponse.fail("模板不存在");
        }
        template.setId(id);
        template.setBuiltin(existing.getBuiltin());
        template.setCreatedAt(existing.getCreatedAt());
        template.setUpdatedAt(LocalDateTime.now());
        layoutTemplateMapper.updateById(template);
        return ApiResponse.ok(template);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        LayoutTemplate existing = layoutTemplateMapper.selectById(id);
        if (existing != null && Boolean.TRUE.equals(existing.getBuiltin())) {
            return ApiResponse.fail("内置模板不可删除");
        }
        layoutTemplateMapper.deleteById(id);
        return ApiResponse.ok(null);
    }
}

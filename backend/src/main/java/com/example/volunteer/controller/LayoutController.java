package com.example.volunteer.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.volunteer.common.ApiResponse;
import com.example.volunteer.entity.Layout;
import com.example.volunteer.mapper.LayoutMapper;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/layouts")
public class LayoutController {

    private final LayoutMapper layoutMapper;

    public LayoutController(LayoutMapper layoutMapper) {
        this.layoutMapper = layoutMapper;
    }

    @PostMapping
    public ApiResponse<Layout> create(@Valid @RequestBody Layout layout) {
        layoutMapper.insert(layout);
        return ApiResponse.ok(layout);
    }

    @GetMapping
    public ApiResponse<List<Layout>> list() {
        LambdaQueryWrapper<Layout> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Layout::getUpdatedAt);
        return ApiResponse.ok(layoutMapper.selectList(wrapper));
    }

    @GetMapping("/{id}")
    public ApiResponse<Layout> get(@PathVariable Long id) {
        return ApiResponse.ok(layoutMapper.selectById(id));
    }

    @PutMapping("/{id}")
    public ApiResponse<Layout> update(@PathVariable Long id, @Valid @RequestBody Layout layout) {
        Layout existing = layoutMapper.selectById(id);
        if (existing == null) {
            return ApiResponse.fail("布局不存在");
        }
        layout.setId(id);
        layoutMapper.updateById(layout);
        return ApiResponse.ok(layout);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        layoutMapper.deleteById(id);
        return ApiResponse.ok(null);
    }
}

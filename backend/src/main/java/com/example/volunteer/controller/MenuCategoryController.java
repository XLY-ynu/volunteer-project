package com.example.volunteer.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.volunteer.common.ApiResponse;
import com.example.volunteer.dto.MenuCategoryRequest;
import com.example.volunteer.entity.MenuCategory;
import com.example.volunteer.mapper.MenuCategoryMapper;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class MenuCategoryController {

    private final MenuCategoryMapper menuCategoryMapper;

    public MenuCategoryController(MenuCategoryMapper menuCategoryMapper) {
        this.menuCategoryMapper = menuCategoryMapper;
    }

    @PostMapping
    public ApiResponse<MenuCategory> create(@Valid @RequestBody MenuCategoryRequest request) {
        MenuCategory c = new MenuCategory();
        c.setName(request.getName());
        c.setCode(request.getCode());
        c.setParentId(request.getParentId());
        c.setSortOrder(request.getSortOrder());
        menuCategoryMapper.insert(c);
        return ApiResponse.ok(c);
    }

    @GetMapping
    public ApiResponse<List<MenuCategory>> list(@RequestParam(required = false) Long parentId) {
        LambdaQueryWrapper<MenuCategory> wrapper = new LambdaQueryWrapper<>();
        if (parentId != null) {
            wrapper.eq(MenuCategory::getParentId, parentId);
        }
        wrapper.orderByAsc(MenuCategory::getSortOrder);
        return ApiResponse.ok(menuCategoryMapper.selectList(wrapper));
    }
}

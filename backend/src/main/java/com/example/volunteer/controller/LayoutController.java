package com.example.volunteer.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.volunteer.common.ApiResponse;
import com.example.volunteer.dto.LayoutAreaPoolRequest;
import com.example.volunteer.entity.Layout;
import com.example.volunteer.entity.LayoutAreaPool;
import com.example.volunteer.mapper.LayoutAreaPoolMapper;
import com.example.volunteer.mapper.LayoutMapper;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/layouts")
public class LayoutController {

    private final LayoutMapper layoutMapper;
    private final LayoutAreaPoolMapper layoutAreaPoolMapper;

    public LayoutController(LayoutMapper layoutMapper, LayoutAreaPoolMapper layoutAreaPoolMapper) {
        this.layoutMapper = layoutMapper;
        this.layoutAreaPoolMapper = layoutAreaPoolMapper;
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

    @GetMapping("/{id}/pools")
    public ApiResponse<List<LayoutAreaPool>> pools(@PathVariable Long id) {
        LambdaQueryWrapper<LayoutAreaPool> w = new LambdaQueryWrapper<>();
        w.eq(LayoutAreaPool::getLayoutId, id).orderByAsc(LayoutAreaPool::getAreaIndex).orderByAsc(LayoutAreaPool::getSortOrder);
        return ApiResponse.ok(layoutAreaPoolMapper.selectList(w));
    }

    @PutMapping("/{id}/pools/{areaIndex}")
    public ApiResponse<List<LayoutAreaPool>> savePool(@PathVariable Long id,
                                                      @PathVariable Integer areaIndex,
                                                      @RequestBody LayoutAreaPoolRequest request) {
        layoutAreaPoolMapper.delete(new LambdaQueryWrapper<LayoutAreaPool>()
                .eq(LayoutAreaPool::getLayoutId, id)
                .eq(LayoutAreaPool::getAreaIndex, areaIndex));
        if (request != null && request.getItems() != null) {
            int i = 0;
            for (LayoutAreaPoolRequest.LayoutAreaPoolItem item : request.getItems()) {
                LayoutAreaPool pool = new LayoutAreaPool();
                pool.setLayoutId(id);
                pool.setAreaIndex(areaIndex);
                pool.setMediaId(item.getMediaId());
                pool.setContentId(item.getContentId());
                pool.setDisplayDuration(item.getDisplayDuration());
                pool.setSortOrder(item.getSortOrder() != null ? item.getSortOrder() : i++);
                pool.setCreatedAt(LocalDateTime.now());
                pool.setUpdatedAt(LocalDateTime.now());
                layoutAreaPoolMapper.insert(pool);
            }
        }
        return pools(id);
    }
}

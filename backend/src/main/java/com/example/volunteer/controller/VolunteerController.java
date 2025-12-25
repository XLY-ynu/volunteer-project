package com.example.volunteer.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.volunteer.common.ApiResponse;
import com.example.volunteer.entity.Volunteer;
import com.example.volunteer.mapper.VolunteerMapper;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/volunteers")
public class VolunteerController {

    private final VolunteerMapper volunteerMapper;

    public VolunteerController(VolunteerMapper volunteerMapper) {
        this.volunteerMapper = volunteerMapper;
    }

    @GetMapping
    public ApiResponse<Page<Volunteer>> list(@RequestParam(defaultValue = "1") int page,
                                             @RequestParam(defaultValue = "20") int size,
                                             @RequestParam(required = false) String name,
                                             @RequestParam(required = false) String status) {
        LambdaQueryWrapper<Volunteer> w = new LambdaQueryWrapper<>();
        if (name != null && !name.isEmpty()) w.like(Volunteer::getName, name);
        if (status != null && !status.isEmpty()) w.eq(Volunteer::getStatus, status);
        Page<Volunteer> p = new Page<>(page, size);
        volunteerMapper.selectPage(p, w);
        return ApiResponse.ok(p);
    }

    @PostMapping
    public ApiResponse<Volunteer> create(@Valid @RequestBody Volunteer volunteer) {
        volunteer.setStatus(volunteer.getStatus() == null ? "pending" : volunteer.getStatus());
        volunteer.setCreatedAt(LocalDateTime.now());
        volunteer.setUpdatedAt(LocalDateTime.now());
        volunteerMapper.insert(volunteer);
        return ApiResponse.ok(volunteer);
    }

    @PutMapping("/{id}")
    public ApiResponse<Volunteer> update(@PathVariable Long id, @Valid @RequestBody Volunteer volunteer) {
        volunteer.setId(id);
        volunteer.setUpdatedAt(LocalDateTime.now());
        volunteerMapper.updateById(volunteer);
        return ApiResponse.ok(volunteer);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        volunteerMapper.deleteById(id);
        return ApiResponse.ok(null);
    }
}

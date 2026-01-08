package com.example.volunteer.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.volunteer.common.ApiResponse;
import com.example.volunteer.dto.LayoutTemplateImportRequest;
import com.example.volunteer.entity.LayoutTemplate;
import com.example.volunteer.entity.LayoutTemplateHistory;
import com.example.volunteer.mapper.LayoutTemplateMapper;
import com.example.volunteer.mapper.LayoutTemplateHistoryMapper;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/layout-templates")
public class LayoutTemplateController {

    private final LayoutTemplateMapper layoutTemplateMapper;
    private final LayoutTemplateHistoryMapper layoutTemplateHistoryMapper;

    public LayoutTemplateController(LayoutTemplateMapper layoutTemplateMapper, LayoutTemplateHistoryMapper layoutTemplateHistoryMapper) {
        this.layoutTemplateMapper = layoutTemplateMapper;
        this.layoutTemplateHistoryMapper = layoutTemplateHistoryMapper;
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
        recordHistory(template);
        return ApiResponse.ok(template);
    }

    @PutMapping("/{id}")
    public ApiResponse<LayoutTemplate> update(@PathVariable Long id, @Valid @RequestBody LayoutTemplate template) {
        LayoutTemplate existing = layoutTemplateMapper.selectById(id);
        if (existing == null) {
            return ApiResponse.fail("模板不存在");
        }
        recordHistory(existing);
        template.setId(id);
        template.setBuiltin(existing.getBuiltin());
        template.setCreatedAt(existing.getCreatedAt());
        template.setUpdatedAt(LocalDateTime.now());
        layoutTemplateMapper.updateById(template);
        recordHistory(template);
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

    @PostMapping("/import")
    public ApiResponse<LayoutTemplate> importTemplate(@Valid @RequestBody LayoutTemplateImportRequest request) {
        LayoutTemplate tpl = new LayoutTemplate();
        tpl.setName(request.getName());
        tpl.setDescription(request.getDescription());
        tpl.setTags(request.getTags());
        tpl.setLayoutJson(request.getLayoutJson());
        tpl.setCoverUrl(request.getCoverUrl());
        tpl.setBuiltin(false);
        tpl.setCreatedAt(LocalDateTime.now());
        tpl.setUpdatedAt(LocalDateTime.now());
        layoutTemplateMapper.insert(tpl);
        recordHistory(tpl);
        return ApiResponse.ok(tpl);
    }

    @PostMapping("/import-file")
    public ApiResponse<LayoutTemplate> importFile(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty() || file.getSize() == 0) {
                return ApiResponse.fail("导入失败: 文件为空");
            }
            String json = new String(file.getBytes());
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            LayoutTemplateImportRequest req = mapper.readValue(json, LayoutTemplateImportRequest.class);
            return importTemplate(req);
        } catch (Exception e) {
            return ApiResponse.fail("导入失败: " + e.getMessage());
        }
    }

    @GetMapping("/{id}/export-file")
    public @ResponseBody byte[] exportTemplateFile(@PathVariable Long id) {
        LayoutTemplate tpl = layoutTemplateMapper.selectById(id);
        if (tpl == null) {
            return new byte[0];
        }
        java.util.Map<String, Object> payload = new java.util.HashMap<>();
        payload.put("name", tpl.getName());
        payload.put("description", tpl.getDescription());
        payload.put("tags", tpl.getTags());
        payload.put("layoutJson", tpl.getLayoutJson());
        payload.put("coverUrl", tpl.getCoverUrl());
        try {
            String json = new com.fasterxml.jackson.databind.ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(payload);
            return json.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            return new byte[0];
        }
    }

    @PostMapping("/cover")
    public ApiResponse<String> uploadCover(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) return ApiResponse.fail("文件为空");
            String filename = UUID.randomUUID() + "-" + file.getOriginalFilename();
            Path uploadDir = Paths.get("uploads");
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }
            Path target = uploadDir.resolve(filename);
            Files.write(target, file.getBytes());
            return ApiResponse.ok("/uploads/" + filename);
        } catch (Exception e) {
            return ApiResponse.fail("上传失败: " + e.getMessage());
        }
    }

    @GetMapping("/{id}/export")
    public ApiResponse<Map<String, Object>> exportTemplate(@PathVariable Long id) {
        LayoutTemplate tpl = layoutTemplateMapper.selectById(id);
        if (tpl == null) {
            return ApiResponse.fail("模板不存在");
        }
        Map<String, Object> payload = new java.util.HashMap<>();
        payload.put("name", tpl.getName());
        payload.put("description", tpl.getDescription());
        payload.put("tags", tpl.getTags());
        payload.put("layoutJson", tpl.getLayoutJson());
        payload.put("coverUrl", tpl.getCoverUrl());
        payload.put("createdAt", tpl.getCreatedAt());
        return ApiResponse.ok(payload);
    }

    @GetMapping("/{id}/history")
    public ApiResponse<List<LayoutTemplateHistory>> history(@PathVariable Long id) {
        LambdaQueryWrapper<LayoutTemplateHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LayoutTemplateHistory::getTemplateId, id)
                .orderByDesc(LayoutTemplateHistory::getVersion)
                .last("limit 30");
        return ApiResponse.ok(layoutTemplateHistoryMapper.selectList(wrapper));
    }

    @PostMapping("/{id}/rollback/{historyId}")
    public ApiResponse<LayoutTemplate> rollback(@PathVariable Long id, @PathVariable Long historyId) {
        LayoutTemplateHistory history = layoutTemplateHistoryMapper.selectById(historyId);
        if (history == null || !id.equals(history.getTemplateId())) {
            return ApiResponse.fail("历史版本不存在");
        }
        LayoutTemplate tpl = layoutTemplateMapper.selectById(id);
        if (tpl == null) {
            return ApiResponse.fail("模板不存在");
        }
        tpl.setName(history.getName());
        tpl.setDescription(history.getDescription());
        tpl.setLayoutJson(history.getLayoutJson());
        tpl.setTags(history.getTags());
        tpl.setCoverUrl(history.getCoverUrl());
        tpl.setUpdatedAt(LocalDateTime.now());
        layoutTemplateMapper.updateById(tpl);
        recordHistory(tpl);
        return ApiResponse.ok(tpl);
    }

    private void recordHistory(LayoutTemplate tpl) {
        if (tpl == null || tpl.getId() == null) {
            return;
        }
        LayoutTemplateHistory latest = layoutTemplateHistoryMapper.selectOne(new LambdaQueryWrapper<LayoutTemplateHistory>()
                .eq(LayoutTemplateHistory::getTemplateId, tpl.getId())
                .orderByDesc(LayoutTemplateHistory::getVersion)
                .last("limit 1"));
        int version = latest != null && latest.getVersion() != null ? latest.getVersion() + 1 : 1;
        LayoutTemplateHistory history = new LayoutTemplateHistory();
        history.setTemplateId(tpl.getId());
        history.setName(tpl.getName());
        history.setDescription(tpl.getDescription());
        history.setLayoutJson(tpl.getLayoutJson());
        history.setTags(tpl.getTags());
        history.setCoverUrl(tpl.getCoverUrl());
        history.setVersion(version);
        history.setCreatedAt(LocalDateTime.now());
        layoutTemplateHistoryMapper.insert(history);
    }
}

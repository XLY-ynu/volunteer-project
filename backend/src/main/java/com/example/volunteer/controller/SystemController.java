/**
 * @Author: 陈力宏
 * @Module: 系统管理 - 系统设置
 * @Description: 系统信息查询与数据备份控制器
 */
package com.example.volunteer.controller;

import com.example.volunteer.common.ApiResponse;
import com.example.volunteer.mapper.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RestController
@RequestMapping("/api/ops")
public class SystemController {

    @Value("${app.storage.root:uploads}")
    private String storageRoot;

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final MenuCategoryMapper menuCategoryMapper;
    private final ContentItemMapper contentItemMapper;
    private final ContentConfigMapper contentConfigMapper;
    private final MediaAssetMapper mediaAssetMapper;
    private final PlaylistMapper playlistMapper;
    private final PlaylistItemMapper playlistItemMapper;
    private final LayoutMapper layoutMapper;
    private final TerminalMapper terminalMapper;
    private final TerminalPlaylistMapper terminalPlaylistMapper;
    private final BroadcastJobMapper broadcastJobMapper;
    private final TerminalGroupRuleMapper terminalGroupRuleMapper;
    private final VolunteerMapper volunteerMapper;
    private final ActivityMapper activityMapper;
    private final ActivitySignupMapper activitySignupMapper;

    public SystemController(
            UserMapper userMapper,
            RoleMapper roleMapper,
            MenuCategoryMapper menuCategoryMapper,
            ContentItemMapper contentItemMapper,
            ContentConfigMapper contentConfigMapper,
            MediaAssetMapper mediaAssetMapper,
            PlaylistMapper playlistMapper,
            PlaylistItemMapper playlistItemMapper,
            LayoutMapper layoutMapper,
            TerminalMapper terminalMapper,
            TerminalPlaylistMapper terminalPlaylistMapper,
            BroadcastJobMapper broadcastJobMapper,
            TerminalGroupRuleMapper terminalGroupRuleMapper,
            VolunteerMapper volunteerMapper,
            ActivityMapper activityMapper,
            ActivitySignupMapper activitySignupMapper) {
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
        this.menuCategoryMapper = menuCategoryMapper;
        this.contentItemMapper = contentItemMapper;
        this.contentConfigMapper = contentConfigMapper;
        this.mediaAssetMapper = mediaAssetMapper;
        this.playlistMapper = playlistMapper;
        this.playlistItemMapper = playlistItemMapper;
        this.layoutMapper = layoutMapper;
        this.terminalMapper = terminalMapper;
        this.terminalPlaylistMapper = terminalPlaylistMapper;
        this.broadcastJobMapper = broadcastJobMapper;
        this.terminalGroupRuleMapper = terminalGroupRuleMapper;
        this.volunteerMapper = volunteerMapper;
        this.activityMapper = activityMapper;
        this.activitySignupMapper = activitySignupMapper;
    }

    @GetMapping("/system-info")
    public ApiResponse<Map<String, Object>> systemInfo() {
        Map<String, Object> map = new HashMap<>();
        map.put("app", "volunteer-platform");
        map.put("time", LocalDateTime.now().toString());
        map.put("java", System.getProperty("java.version"));
        map.put("os", System.getProperty("os.name"));
        map.put("storageRoot", Paths.get(storageRoot).toAbsolutePath().toString());
        return ApiResponse.ok(map);
    }

    @GetMapping("/backup")
    public ResponseEntity<InputStreamResource> backup() throws IOException {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(bos)) {
            
            // 1. 导出数据库数据为JSON
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            
            Map<String, Object> dbData = new LinkedHashMap<>();
            dbData.put("exportTime", LocalDateTime.now().toString());
            dbData.put("version", "1.0");
            
            // 用户和角色
            dbData.put("users", userMapper.selectList(null));
            dbData.put("roles", roleMapper.selectList(null));
            
            // 内容管理
            dbData.put("categories", menuCategoryMapper.selectList(null));
            dbData.put("contents", contentItemMapper.selectList(null));
            dbData.put("contentConfig", contentConfigMapper.selectList(null));
            dbData.put("mediaAssets", mediaAssetMapper.selectList(null));
            
            // 播放管理
            dbData.put("layouts", layoutMapper.selectList(null));
            dbData.put("playlists", playlistMapper.selectList(null));
            dbData.put("playlistItems", playlistItemMapper.selectList(null));
            
            // 终端管理
            dbData.put("terminals", terminalMapper.selectList(null));
            dbData.put("terminalPlaylists", terminalPlaylistMapper.selectList(null));
            dbData.put("broadcasts", broadcastJobMapper.selectList(null));
            dbData.put("terminalGroupRules", terminalGroupRuleMapper.selectList(null));
            
            // 志愿者管理
            dbData.put("volunteers", volunteerMapper.selectList(null));
            dbData.put("activities", activityMapper.selectList(null));
            dbData.put("activitySignups", activitySignupMapper.selectList(null));
            
            String jsonData = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(dbData);
            addStringToZip(zos, jsonData, "data/database.json");
            
            // 2. 添加schema.sql
            Path schemaPath = Paths.get("src/main/resources/schema.sql");
            if (Files.exists(schemaPath)) {
                addFileToZip(zos, schemaPath, "data/schema.sql");
            }
            
            // 3. 备份上传的媒体文件（包括子目录）
            Path uploadPath = Paths.get(storageRoot).toAbsolutePath();
            if (Files.exists(uploadPath)) {
                addDirectoryToZip(zos, uploadPath, "uploads");
            }
            
            // 4. 生成备份信息文件
            Map<String, Object> backupInfo = new LinkedHashMap<>();
            backupInfo.put("backupTime", LocalDateTime.now().toString());
            backupInfo.put("platform", "volunteer-platform");
            backupInfo.put("javaVersion", System.getProperty("java.version"));
            backupInfo.put("osName", System.getProperty("os.name"));
            
            // 统计信息
            Map<String, Integer> stats = new LinkedHashMap<>();
            stats.put("users", userMapper.selectList(null).size());
            stats.put("categories", menuCategoryMapper.selectList(null).size());
            stats.put("contents", contentItemMapper.selectList(null).size());
            stats.put("mediaAssets", mediaAssetMapper.selectList(null).size());
            stats.put("playlists", playlistMapper.selectList(null).size());
            stats.put("terminals", terminalMapper.selectList(null).size());
            stats.put("volunteers", volunteerMapper.selectList(null).size());
            stats.put("activities", activityMapper.selectList(null).size());
            backupInfo.put("statistics", stats);
            
            String infoJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(backupInfo);
            addStringToZip(zos, infoJson, "backup-info.json");
        }
        
        byte[] bytes = bos.toByteArray();
        InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream(bytes));
        String filename = "backup_" + timestamp + ".zip";
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentLength(bytes.length)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    private void addFileToZip(ZipOutputStream zos, Path path, String entryName) throws IOException {
        if (!Files.exists(path)) return;
        zos.putNextEntry(new ZipEntry(entryName));
        Files.copy(path, zos);
        zos.closeEntry();
    }
    
    private void addStringToZip(ZipOutputStream zos, String content, String entryName) throws IOException {
        zos.putNextEntry(new ZipEntry(entryName));
        zos.write(content.getBytes(StandardCharsets.UTF_8));
        zos.closeEntry();
    }
    
    private void addDirectoryToZip(ZipOutputStream zos, Path dir, String basePath) throws IOException {
        Files.walk(dir).forEach(path -> {
            try {
                if (Files.isRegularFile(path)) {
                    String relativePath = dir.relativize(path).toString().replace("\\", "/");
                    String entryName = basePath + "/" + relativePath;
                    addFileToZip(zos, path, entryName);
                }
            } catch (IOException ignored) {
            }
        });
    }
}

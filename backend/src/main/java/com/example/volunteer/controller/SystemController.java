package com.example.volunteer.controller;

import com.example.volunteer.common.ApiResponse;
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
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RestController
@RequestMapping("/api/ops")
public class SystemController {

    @Value("${app.storage.root:uploads}")
    private String storageRoot;

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
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(bos)) {
            addFileToZip(zos, Paths.get("src/main/resources/schema.sql"), "schema.sql");
            Path uploadPath = Paths.get(storageRoot).toAbsolutePath();
            if (Files.exists(uploadPath)) {
                Files.list(uploadPath).filter(Files::isRegularFile).forEach(p -> {
                    try {
                        addFileToZip(zos, p, "uploads/" + p.getFileName());
                    } catch (IOException ignored) {
                    }
                });
            }
        }
        byte[] bytes = bos.toByteArray();
        InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream(bytes));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=backup.zip")
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
}

package com.example.volunteer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.volunteer.dto.MediaAssetRequest;
import com.example.volunteer.entity.MediaAsset;
import com.example.volunteer.mapper.MediaAssetMapper;
import com.example.volunteer.service.MediaAssetService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class MediaAssetServiceImpl implements MediaAssetService {

    private final MediaAssetMapper mediaAssetMapper;
    @Value("${app.storage.root:uploads}")
    private String storageRoot;

    public MediaAssetServiceImpl(MediaAssetMapper mediaAssetMapper) {
        this.mediaAssetMapper = mediaAssetMapper;
    }

    @Override
    public MediaAsset create(MediaAssetRequest request) {
        MediaAsset asset = new MediaAsset();
        asset.setName(request.getName());
        asset.setType(request.getType());
        asset.setUrl(request.getUrl());
        asset.setThumbUrl(request.getThumbUrl());
        asset.setSizeBytes(request.getSizeBytes());
        asset.setDurationSeconds(request.getDurationSeconds());
        asset.setWidth(request.getWidth());
        asset.setHeight(request.getHeight());
        asset.setBitrateKbps(request.getBitrateKbps());
        asset.setFrameRate(request.getFrameRate());
        asset.setChecksum(request.getChecksum());
        asset.setCreatedAt(LocalDateTime.now());
        mediaAssetMapper.insert(asset);
        return asset;
    }

    @Override
    public Page<MediaAsset> page(int page, int size, String type) {
        LambdaQueryWrapper<MediaAsset> wrapper = new LambdaQueryWrapper<>();
        if (type != null && !type.isEmpty()) {
            wrapper.eq(MediaAsset::getType, type);
        }
        Page<MediaAsset> p = new Page<>(page, size);
        mediaAssetMapper.selectPage(p, wrapper);
        return p;
    }

    @Override
    public void delete(Long id) {
        MediaAsset asset = mediaAssetMapper.selectById(id);
        mediaAssetMapper.deleteById(id);
        if (asset != null && asset.getUrl() != null && asset.getUrl().startsWith("/uploads/")) {
            try {
                Path root = Paths.get(storageRoot).toAbsolutePath();
                String filename = asset.getUrl().replaceFirst("^/uploads/", "");
                Path dest = root.resolve(filename);
                Files.deleteIfExists(dest);
            } catch (Exception ignored) {
                // 删除物理文件失败不影响主流程
            }
        }
    }

    @Override
    public MediaAsset upload(MultipartFile file, String type) {
        try {
            String original = file.getOriginalFilename();
            String ext = (original != null && original.contains(".")) ? original.substring(original.lastIndexOf('.')) : "";
            String filename = UUID.randomUUID() + ext;
            Path root = Paths.get(storageRoot).toAbsolutePath();
            Files.createDirectories(root);
            Path dest = root.resolve(filename);
            file.transferTo(dest.toFile());

            String resolvedType = type != null ? type : guessType(file.getContentType());
            MediaAsset asset = new MediaAsset();
            asset.setName(original != null ? original : filename);
            asset.setType(resolvedType);
            asset.setUrl("/uploads/" + filename);
            asset.setThumbUrl("image".equals(resolvedType) ? asset.getUrl() : null);
            asset.setSizeBytes(file.getSize());
            fillMetadata(asset, dest, resolvedType);
            asset.setCreatedAt(LocalDateTime.now());
            mediaAssetMapper.insert(asset);
            return asset;
        } catch (Exception e) {
            throw new RuntimeException("上传失败", e);
        }
    }

    @Override
    public MediaAsset findById(Long id) {
        return mediaAssetMapper.selectById(id);
    }

    @Override
    public MediaAsset uploadThumb(Long id, MultipartFile file) {
        MediaAsset asset = mediaAssetMapper.selectById(id);
        if (asset == null) {
            throw new IllegalArgumentException("媒体不存在");
        }
        try {
            String original = file.getOriginalFilename();
            String ext = (original != null && original.contains(".")) ? original.substring(original.lastIndexOf('.')) : "";
            String filename = "thumb-" + UUID.randomUUID() + ext;
            Path root = Paths.get(storageRoot).toAbsolutePath().resolve("thumbs");
            Files.createDirectories(root);
            Path dest = root.resolve(filename);
            file.transferTo(dest.toFile());
            asset.setThumbUrl("/uploads/thumbs/" + filename);
            mediaAssetMapper.updateById(asset);
            return asset;
        } catch (Exception e) {
            throw new RuntimeException("上传封面失败", e);
        }
    }

    private String guessType(String contentType) {
        if (contentType == null) {
            return "unknown";
        }
        if (contentType.startsWith("video")) return "video";
        if (contentType.startsWith("image")) return "image";
        if (contentType.contains("pdf")) return "document";
        return "other";
    }

    private void fillMetadata(MediaAsset asset, Path path, String type) {
        try {
            if ("image".equals(type)) {
                BufferedImage img = ImageIO.read(path.toFile());
                if (img != null) {
                    asset.setWidth(img.getWidth());
                    asset.setHeight(img.getHeight());
                }
            }
            if ("video".equals(type)) {
                VideoMeta meta = probeVideo(path);
                if (meta != null) {
                    asset.setWidth(meta.width);
                    asset.setHeight(meta.height);
                    asset.setDurationSeconds((int) Math.round(meta.duration));
                    asset.setBitrateKbps(meta.bitrateKbps);
                    asset.setFrameRate(meta.frameRate);
                }
            }
        } catch (Exception ignored) {
            // 元数据读取失败不影响主流程
        }
    }

    private VideoMeta probeVideo(Path path) {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "ffprobe",
                    "-v", "error",
                    "-select_streams", "v:0",
                    "-show_entries", "stream=width,height,duration,bit_rate,r_frame_rate",
                    "-of", "default=noprint_wrappers=1:nokey=1",
                    path.toString()
            );
            pb.redirectErrorStream(true);
            Process p = pb.start();
            byte[] bytes = p.getInputStream().readAllBytes();
            p.waitFor(2, TimeUnit.SECONDS);
            if (p.isAlive()) {
                p.destroy();
            }
            String out = new String(bytes, StandardCharsets.UTF_8).trim();
            if (out.isEmpty()) {
                return null;
            }
            String[] parts = out.split("\\R");
            if (parts.length < 2) {
                return null;
            }
            int width = Integer.parseInt(parts[0].trim());
            int height = Integer.parseInt(parts[1].trim());
            double duration = parts.length >= 3 ? Double.parseDouble(parts[2].trim()) : 0;
            Integer bitrateKbps = null;
            if (parts.length >= 4 && !parts[3].trim().isEmpty()) {
                try {
                    bitrateKbps = (int) Math.round(Double.parseDouble(parts[3].trim()) / 1000.0);
                } catch (NumberFormatException ignored) {
                    bitrateKbps = null;
                }
            }
            Double frameRate = null;
            if (parts.length >= 5 && !parts[4].trim().isEmpty()) {
                frameRate = parseFrameRate(parts[4].trim());
            }
            return new VideoMeta(width, height, duration, bitrateKbps, frameRate);
        } catch (Exception e) {
            return null;
        }
    }

    private Double parseFrameRate(String value) {
        try {
            if (value.contains("/")) {
                String[] parts = value.split("/");
                double numerator = Double.parseDouble(parts[0]);
                double denominator = Double.parseDouble(parts[1]);
                if (denominator == 0) return null;
                return numerator / denominator;
            }
            return Double.parseDouble(value);
        } catch (Exception e) {
            return null;
        }
    }

    private static class VideoMeta {
        final int width;
        final int height;
        final double duration;
        final Integer bitrateKbps;
        final Double frameRate;

        VideoMeta(int width, int height, double duration, Integer bitrateKbps, Double frameRate) {
            this.width = width;
            this.height = height;
            this.duration = duration;
            this.bitrateKbps = bitrateKbps;
            this.frameRate = frameRate;
        }
    }

    @Override
    public String uploadCoverOnly(MultipartFile file) {
        try {
            String original = file.getOriginalFilename();
            String ext = (original != null && original.contains(".")) ? original.substring(original.lastIndexOf('.')) : "";
            String filename = "cover-" + UUID.randomUUID() + ext;
            Path root = Paths.get(storageRoot).toAbsolutePath().resolve("covers");
            Files.createDirectories(root);
            Path dest = root.resolve(filename);
            file.transferTo(dest.toFile());
            return "/uploads/covers/" + filename;
        } catch (Exception e) {
            throw new RuntimeException("上传封面失败", e);
        }
    }
}

package com.example.volunteer.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("media_asset")
public class MediaAsset {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String type; // video, image, document, web
    private String url;
    private Long sizeBytes;
    private Integer durationSeconds;
    private Integer width;
    private Integer height;
    private String checksum;
    private LocalDateTime createdAt;
}

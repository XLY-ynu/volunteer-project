package com.example.volunteer.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("playlist")
public class Playlist {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String description;
    private String coverUrl;
    private Long layoutId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

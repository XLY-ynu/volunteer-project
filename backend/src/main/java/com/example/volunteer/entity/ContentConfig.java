package com.example.volunteer.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("content_config")
public class ContentConfig {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Integer recommendIntervalSec;
    private Integer previewIntervalSec;
    private LocalDateTime updatedAt;
}

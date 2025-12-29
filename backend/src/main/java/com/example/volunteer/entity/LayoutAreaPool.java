package com.example.volunteer.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("layout_area_pool")
public class LayoutAreaPool {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long layoutId;
    private Integer areaIndex;
    private Long mediaId;
    private Long contentId;
    private Integer displayDuration;
    private Integer sortOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

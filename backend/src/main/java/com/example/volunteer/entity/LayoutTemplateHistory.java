package com.example.volunteer.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("layout_template_history")
public class LayoutTemplateHistory {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long templateId;
    private String name;
    private String description;
    private String layoutJson;
    private String tags;
    private String coverUrl;
    private Integer version;
    private LocalDateTime createdAt;
}

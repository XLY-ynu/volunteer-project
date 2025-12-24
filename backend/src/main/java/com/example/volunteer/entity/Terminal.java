package com.example.volunteer.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("terminal")
public class Terminal {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String code;
    private String name;
    private String groupName;
    private String status;
    private LocalDateTime lastHeartbeat;
    private String attributes; // store JSON string
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

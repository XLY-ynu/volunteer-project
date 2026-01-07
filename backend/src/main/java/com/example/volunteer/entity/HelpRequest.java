package com.example.volunteer.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("help_request")
public class HelpRequest {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long orgId;
    private String title;
    private String content;
    private String contactName;
    private String contactPhone;
    private String address;
    private String status; // pending, processing, completed, rejected
    private String reply;
    private LocalDateTime repliedAt;
    private Long repliedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

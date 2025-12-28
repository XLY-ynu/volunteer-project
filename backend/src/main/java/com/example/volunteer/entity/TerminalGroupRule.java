package com.example.volunteer.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("terminal_group_rule")
public class TerminalGroupRule {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String groupName;
    private Integer offlineThreshold;
    private Boolean enabled;
    private String notifyChannel;
    private String notifyTarget;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

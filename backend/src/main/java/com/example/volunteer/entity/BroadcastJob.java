package com.example.volunteer.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("broadcast_job")
public class BroadcastJob {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private Long mediaId;
    private Long contentId;
    private String targetGroup;
    private String targetTerminalCode;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
    private Integer priority;
    private String queueMode;
}

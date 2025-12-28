package com.example.volunteer.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("alert_silence")
public class AlertSilence {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String groupName;
    private String channel;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Boolean enabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

package com.example.volunteer.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("notification_log")
public class NotificationLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String channel;
    private String target;
    private String title;
    private String content;
    private String status;
    private LocalDateTime createdAt;
}

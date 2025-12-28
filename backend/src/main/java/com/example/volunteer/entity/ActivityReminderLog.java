package com.example.volunteer.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("activity_reminder_log")
public class ActivityReminderLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long volunteerId;
    private Long activityId;
    private String reminderType;
    private String channel;
    private String status;
    private String message;
    private LocalDateTime createdAt;
}

package com.example.volunteer.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("activity_checkin_log")
public class ActivityCheckinLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long activityId;
    private Long volunteerId;
    private LocalDateTime createdAt;
}

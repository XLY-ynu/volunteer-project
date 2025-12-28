package com.example.volunteer.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("volunteer_reminder_setting")
public class VolunteerReminderSetting {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long volunteerId;
    private Boolean signupReminder;
    private Boolean checkinReminder;
    private String channel;
    private Integer reminderMinutes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

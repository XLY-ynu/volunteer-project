package com.example.volunteer.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("portal_message_read")
public class PortalMessageRead {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long volunteerId;
    private String messageKey;
    private LocalDateTime readAt;
}

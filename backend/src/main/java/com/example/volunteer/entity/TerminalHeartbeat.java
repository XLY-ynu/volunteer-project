package com.example.volunteer.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("terminal_heartbeat")
public class TerminalHeartbeat {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long terminalId;
    private String status;
    private LocalDateTime createdAt;
}

package com.example.volunteer.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("terminal_alert_history")
public class TerminalAlertHistory {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String groupName;
    private Integer total;
    private Integer offline;
    private Integer ruleThreshold;
    private String channel;
    private String target;
    private Boolean silenced;
    private LocalDateTime createdAt;
}

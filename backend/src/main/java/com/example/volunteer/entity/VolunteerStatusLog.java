package com.example.volunteer.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("volunteer_status_log")
public class VolunteerStatusLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long volunteerId;
    private String status;
    private String remark;
    private LocalDateTime createdAt;
}

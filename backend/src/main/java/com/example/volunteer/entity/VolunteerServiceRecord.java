package com.example.volunteer.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("volunteer_service_record")
public class VolunteerServiceRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long volunteerId;
    private Long activityId;
    private Long orgId;
    private BigDecimal serviceHours;
    private LocalDate serviceDate;
    private String remark;
    private LocalDateTime createdAt;
}

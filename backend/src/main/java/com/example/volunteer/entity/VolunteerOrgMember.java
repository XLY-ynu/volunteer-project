package com.example.volunteer.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("volunteer_org_member")
public class VolunteerOrgMember {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long volunteerId;
    private Long orgId;
    private String status; // pending, approved, rejected
    private LocalDateTime joinedAt;
    private LocalDateTime createdAt;
}

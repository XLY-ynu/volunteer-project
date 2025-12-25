package com.example.volunteer.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("volunteer")
public class Volunteer {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String phone;
    private String email;
    private String organization;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

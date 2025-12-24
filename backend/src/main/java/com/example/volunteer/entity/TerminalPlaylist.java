package com.example.volunteer.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("terminal_playlist")
public class TerminalPlaylist {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long terminalId;
    private Long playlistId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Boolean active;
}

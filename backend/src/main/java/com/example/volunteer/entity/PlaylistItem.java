package com.example.volunteer.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("playlist_item")
public class PlaylistItem {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long playlistId;
    private Long mediaId;
    private Long contentId;
    private Integer displayDuration;
    private Integer sortOrder;
    private Integer areaIndex;
}

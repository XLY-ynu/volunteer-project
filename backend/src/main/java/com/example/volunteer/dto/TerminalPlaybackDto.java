package com.example.volunteer.dto;

import com.example.volunteer.entity.Layout;
import com.example.volunteer.entity.MediaAsset;
import com.example.volunteer.entity.ContentItem;
import com.example.volunteer.entity.Playlist;
import com.example.volunteer.entity.PlaylistItem;
import com.example.volunteer.entity.LayoutAreaPool;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class TerminalPlaybackDto {
    private Playlist playlist;
    private Layout layout;
    private List<PlaylistItem> items;
    private List<MediaAsset> mediaAssets;
    private List<ContentItem> contentAssets;
    private List<LayoutAreaPool> areaPools;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}

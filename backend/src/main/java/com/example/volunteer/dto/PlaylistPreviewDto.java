package com.example.volunteer.dto;

import com.example.volunteer.entity.Layout;
import com.example.volunteer.entity.MediaAsset;
import com.example.volunteer.entity.Playlist;
import com.example.volunteer.entity.PlaylistItem;
import lombok.Data;

import java.util.List;

@Data
public class PlaylistPreviewDto {
    private Playlist playlist;
    private Layout layout;
    private List<PlaylistItem> items;
    private List<MediaAsset> mediaAssets;
}

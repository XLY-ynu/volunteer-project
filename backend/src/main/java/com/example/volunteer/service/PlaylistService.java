package com.example.volunteer.service;

import com.example.volunteer.dto.PlaylistPreviewDto;
import com.example.volunteer.dto.PlaylistRequest;
import com.example.volunteer.entity.Playlist;
import com.example.volunteer.entity.PlaylistItem;

import java.util.List;

public interface PlaylistService {
    Playlist create(PlaylistRequest request);
    Playlist update(Long id, PlaylistRequest request);
    List<Playlist> list();
    void delete(Long id);
    List<PlaylistItem> items(Long playlistId);
    PlaylistPreviewDto preview(Long playlistId);
}

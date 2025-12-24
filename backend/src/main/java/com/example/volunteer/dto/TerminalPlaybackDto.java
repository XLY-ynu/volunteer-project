package com.example.volunteer.dto;

import com.example.volunteer.entity.Playlist;
import com.example.volunteer.entity.PlaylistItem;
import com.example.volunteer.entity.Layout;
import lombok.Data;

import java.util.List;

@Data
public class TerminalPlaybackDto {
    private Playlist playlist;
    private Layout layout;
    private List<PlaylistItem> items;
}

package com.example.volunteer.service;

import com.example.volunteer.dto.PlaylistRequest;
import com.example.volunteer.entity.Playlist;

import java.util.List;

public interface PlaylistService {
    Playlist create(PlaylistRequest request);
    List<Playlist> list();
    void delete(Long id);
}

package com.example.volunteer.controller;

import com.example.volunteer.common.ApiResponse;
import com.example.volunteer.dto.PlaylistRequest;
import com.example.volunteer.entity.Playlist;
import com.example.volunteer.entity.PlaylistItem;
import com.example.volunteer.service.PlaylistService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/playlists")
public class PlaylistController {

    private final PlaylistService playlistService;

    public PlaylistController(PlaylistService playlistService) {
        this.playlistService = playlistService;
    }

    @PostMapping
    public ApiResponse<Playlist> create(@Valid @RequestBody PlaylistRequest request) {
        return ApiResponse.ok(playlistService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<Playlist> update(@PathVariable Long id, @Valid @RequestBody PlaylistRequest request) {
        return ApiResponse.ok(playlistService.update(id, request));
    }

    @GetMapping
    public ApiResponse<List<Playlist>> list() {
        return ApiResponse.ok(playlistService.list());
    }

    @GetMapping("/{id}/items")
    public ApiResponse<List<PlaylistItem>> items(@PathVariable Long id) {
        return ApiResponse.ok(playlistService.items(id));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        playlistService.delete(id);
        return ApiResponse.ok(null);
    }
}

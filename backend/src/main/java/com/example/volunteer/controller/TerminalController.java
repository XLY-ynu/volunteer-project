package com.example.volunteer.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.volunteer.common.ApiResponse;
import com.example.volunteer.dto.HeartbeatRequest;
import com.example.volunteer.dto.TerminalPlaylistBindRequest;
import com.example.volunteer.dto.TerminalRequest;
import com.example.volunteer.entity.Terminal;
import com.example.volunteer.entity.TerminalPlaylist;
import com.example.volunteer.service.TerminalService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/terminals")
public class TerminalController {

    private final TerminalService terminalService;

    public TerminalController(TerminalService terminalService) {
        this.terminalService = terminalService;
    }

    @PostMapping
    public ApiResponse<Terminal> register(@Valid @RequestBody TerminalRequest request) {
        return ApiResponse.ok(terminalService.register(request));
    }

    @GetMapping
    public ApiResponse<Page<Terminal>> page(@RequestParam(defaultValue = "1") int page,
                                            @RequestParam(defaultValue = "10") int size,
                                            @RequestParam(required = false) String groupName) {
        return ApiResponse.ok(terminalService.page(page, size, groupName));
    }

    @PostMapping("/heartbeat")
    public ApiResponse<Terminal> heartbeat(@Valid @RequestBody HeartbeatRequest request) {
        return ApiResponse.ok(terminalService.heartbeat(request));
    }

    @PostMapping("/bind-playlist")
    public ApiResponse<Void> bind(@Valid @RequestBody TerminalPlaylistBindRequest request) {
        terminalService.bindPlaylists(request);
        return ApiResponse.ok(null);
    }

    @GetMapping("/{id}/playlists")
    public ApiResponse<List<TerminalPlaylist>> playlists(@PathVariable Long id) {
        return ApiResponse.ok(terminalService.playlists(id));
    }
}

package com.example.volunteer.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.volunteer.dto.HeartbeatRequest;
import com.example.volunteer.dto.TerminalPlaylistBindRequest;
import com.example.volunteer.dto.TerminalRequest;
import com.example.volunteer.entity.Terminal;
import com.example.volunteer.entity.TerminalPlaylist;

public interface TerminalService {
    Terminal register(TerminalRequest request);
    Page<Terminal> page(int page, int size, String groupName);
    Terminal heartbeat(HeartbeatRequest request);
    void bindPlaylists(TerminalPlaylistBindRequest request);
    java.util.List<TerminalPlaylist> playlists(Long terminalId);
}

package com.example.volunteer.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.volunteer.dto.HeartbeatRequest;
import com.example.volunteer.dto.TerminalPlaylistBindRequest;
import com.example.volunteer.dto.TerminalRequest;
import com.example.volunteer.dto.TerminalPlaybackDto;
import com.example.volunteer.dto.TerminalGroupBindRequest;
import com.example.volunteer.entity.Terminal;
import com.example.volunteer.entity.TerminalPlaylist;
import com.example.volunteer.entity.TerminalHeartbeat;

public interface TerminalService {
    Terminal register(TerminalRequest request);
    Page<Terminal> page(int page, int size, String groupName);
    Terminal heartbeat(HeartbeatRequest request);
    void bindPlaylists(TerminalPlaylistBindRequest request);
    void bindPlaylistToGroup(TerminalGroupBindRequest request);
    java.util.List<TerminalPlaylist> playlists(Long terminalId);
    Page<TerminalHeartbeat> heartbeatLogs(Long terminalId, int page, int size);
    java.util.List<TerminalPlaybackDto> playbackForTerminal(String code);
    Terminal updateAttributes(Long id, String attributes);
}

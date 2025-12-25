package com.example.volunteer.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.volunteer.common.ApiResponse;
import com.example.volunteer.entity.Terminal;
import com.example.volunteer.mapper.ActivityMapper;
import com.example.volunteer.mapper.MediaAssetMapper;
import com.example.volunteer.mapper.PlaylistMapper;
import com.example.volunteer.mapper.TerminalMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/monitor")
public class MonitorController {

    private final TerminalMapper terminalMapper;
    private final MediaAssetMapper mediaAssetMapper;
    private final PlaylistMapper playlistMapper;
    private final ActivityMapper activityMapper;
    private final long offlineSeconds;

    public MonitorController(TerminalMapper terminalMapper, MediaAssetMapper mediaAssetMapper,
                             PlaylistMapper playlistMapper, ActivityMapper activityMapper,
                             @Value("${app.terminal.offline-seconds:300}") long offlineSeconds) {
        this.terminalMapper = terminalMapper;
        this.mediaAssetMapper = mediaAssetMapper;
        this.playlistMapper = playlistMapper;
        this.activityMapper = activityMapper;
        this.offlineSeconds = offlineSeconds;
    }

    @GetMapping("/summary")
    public ApiResponse<Map<String, Object>> summary() {
        Map<String, Object> map = new HashMap<>();
        map.put("terminalTotal", terminalMapper.selectCount(new QueryWrapper<>()));
        map.put("mediaTotal", mediaAssetMapper.selectCount(new QueryWrapper<>()));
        map.put("playlistTotal", playlistMapper.selectCount(new QueryWrapper<>()));
        map.put("activityTotal", activityMapper.selectCount(new QueryWrapper<>()));
        return ApiResponse.ok(map);
    }

    @GetMapping("/terminal-status")
    public ApiResponse<Map<String, Object>> terminalStatus() {
        List<Terminal> terminals = terminalMapper.selectList(new QueryWrapper<>());
        LocalDateTime threshold = LocalDateTime.now().minusSeconds(offlineSeconds);
        List<Terminal> offline = terminals.stream()
                .filter(t -> t.getLastHeartbeat() != null && t.getLastHeartbeat().isBefore(threshold))
                .collect(Collectors.toList());
        Map<String, Object> map = new HashMap<>();
        map.put("online", terminals.size() - offline.size());
        map.put("offline", offline.size());
        map.put("offlineTerminals", offline.stream().map(t -> {
            Map<String, Object> o = new HashMap<>();
            o.put("id", t.getId());
            o.put("name", t.getName());
            o.put("code", t.getCode());
            o.put("lastHeartbeat", t.getLastHeartbeat());
            return o;
        }).collect(Collectors.toList()));
        return ApiResponse.ok(map);
    }
}

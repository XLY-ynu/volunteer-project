package com.example.volunteer.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.volunteer.common.ApiResponse;
import com.example.volunteer.mapper.ActivityMapper;
import com.example.volunteer.mapper.MediaAssetMapper;
import com.example.volunteer.mapper.PlaylistMapper;
import com.example.volunteer.mapper.TerminalMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/monitor")
public class MonitorController {

    private final TerminalMapper terminalMapper;
    private final MediaAssetMapper mediaAssetMapper;
    private final PlaylistMapper playlistMapper;
    private final ActivityMapper activityMapper;

    public MonitorController(TerminalMapper terminalMapper, MediaAssetMapper mediaAssetMapper,
                             PlaylistMapper playlistMapper, ActivityMapper activityMapper) {
        this.terminalMapper = terminalMapper;
        this.mediaAssetMapper = mediaAssetMapper;
        this.playlistMapper = playlistMapper;
        this.activityMapper = activityMapper;
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
}

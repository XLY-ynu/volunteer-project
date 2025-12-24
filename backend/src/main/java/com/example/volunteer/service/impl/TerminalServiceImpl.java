package com.example.volunteer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.volunteer.dto.HeartbeatRequest;
import com.example.volunteer.dto.TerminalPlaylistBindRequest;
import com.example.volunteer.dto.TerminalRequest;
import com.example.volunteer.dto.TerminalPlaybackDto;
import com.example.volunteer.entity.Terminal;
import com.example.volunteer.entity.TerminalPlaylist;
import com.example.volunteer.entity.TerminalHeartbeat;
import com.example.volunteer.entity.Playlist;
import com.example.volunteer.entity.PlaylistItem;
import com.example.volunteer.entity.Layout;
import com.example.volunteer.mapper.TerminalMapper;
import com.example.volunteer.mapper.TerminalPlaylistMapper;
import com.example.volunteer.mapper.TerminalHeartbeatMapper;
import com.example.volunteer.mapper.PlaylistMapper;
import com.example.volunteer.mapper.PlaylistItemMapper;
import com.example.volunteer.mapper.LayoutMapper;
import com.example.volunteer.service.TerminalService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TerminalServiceImpl implements TerminalService {

    private final TerminalMapper terminalMapper;
    private final TerminalPlaylistMapper terminalPlaylistMapper;
    private final TerminalHeartbeatMapper terminalHeartbeatMapper;
    private final PlaylistMapper playlistMapper;
    private final PlaylistItemMapper playlistItemMapper;
    private final LayoutMapper layoutMapper;

    public TerminalServiceImpl(TerminalMapper terminalMapper, TerminalPlaylistMapper terminalPlaylistMapper,
                               TerminalHeartbeatMapper terminalHeartbeatMapper,
                               PlaylistMapper playlistMapper,
                               PlaylistItemMapper playlistItemMapper,
                               LayoutMapper layoutMapper) {
        this.terminalMapper = terminalMapper;
        this.terminalPlaylistMapper = terminalPlaylistMapper;
        this.terminalHeartbeatMapper = terminalHeartbeatMapper;
        this.playlistMapper = playlistMapper;
        this.playlistItemMapper = playlistItemMapper;
        this.layoutMapper = layoutMapper;
    }

    @Override
    public Terminal register(TerminalRequest request) {
        Terminal existing = terminalMapper.selectOne(new LambdaQueryWrapper<Terminal>().eq(Terminal::getCode, request.getCode()));
        if (existing != null) {
            existing.setName(request.getName());
            existing.setGroupName(request.getGroupName());
            existing.setAttributes(request.getAttributes());
            existing.setUpdatedAt(LocalDateTime.now());
            terminalMapper.updateById(existing);
            return existing;
        }
        Terminal t = new Terminal();
        t.setCode(request.getCode());
        t.setName(request.getName());
        t.setGroupName(request.getGroupName());
        t.setAttributes(request.getAttributes());
        t.setStatus("online");
        t.setLastHeartbeat(LocalDateTime.now());
        t.setCreatedAt(LocalDateTime.now());
        t.setUpdatedAt(LocalDateTime.now());
        terminalMapper.insert(t);
        return t;
    }

    @Override
    public Page<Terminal> page(int page, int size, String groupName) {
        LambdaQueryWrapper<Terminal> wrapper = new LambdaQueryWrapper<>();
        if (groupName != null && !groupName.isEmpty()) {
            wrapper.eq(Terminal::getGroupName, groupName);
        }
        Page<Terminal> p = new Page<>(page, size);
        terminalMapper.selectPage(p, wrapper);
        long offlineSec = getOfflineSeconds();
        LocalDateTime threshold = LocalDateTime.now().minusSeconds(offlineSec);
        p.getRecords().forEach(t -> {
            if (t.getLastHeartbeat() != null && t.getLastHeartbeat().isBefore(threshold)) {
                t.setStatus("offline");
            }
        });
        return p;
    }

    @Override
    public Terminal heartbeat(HeartbeatRequest request) {
        Terminal t = terminalMapper.selectOne(new LambdaQueryWrapper<Terminal>().eq(Terminal::getCode, request.getCode()));
        if (t == null) {
            t = new Terminal();
            t.setCode(request.getCode());
            t.setName(request.getCode());
            t.setStatus(request.getStatus());
            t.setLastHeartbeat(LocalDateTime.now());
            t.setCreatedAt(LocalDateTime.now());
            t.setUpdatedAt(LocalDateTime.now());
            terminalMapper.insert(t);
        } else {
            t.setStatus(request.getStatus());
            t.setLastHeartbeat(LocalDateTime.now());
            t.setUpdatedAt(LocalDateTime.now());
            terminalMapper.updateById(t);
        }
        TerminalHeartbeat hb = new TerminalHeartbeat();
        hb.setTerminalId(t.getId());
        hb.setStatus(request.getStatus());
        hb.setCreatedAt(LocalDateTime.now());
        terminalHeartbeatMapper.insert(hb);
        return t;
    }

    @Override
    @Transactional
    public void bindPlaylists(TerminalPlaylistBindRequest request) {
        for (Long terminalId : request.getTerminalIds()) {
            TerminalPlaylist tp = new TerminalPlaylist();
            tp.setTerminalId(terminalId);
            tp.setPlaylistId(request.getPlaylistId());
            tp.setStartTime(request.getStartTime());
            tp.setEndTime(request.getEndTime());
            tp.setActive(true);
            terminalPlaylistMapper.insert(tp);
        }
    }

    @Override
    public List<TerminalPlaylist> playlists(Long terminalId) {
        return terminalPlaylistMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<TerminalPlaylist>()
                        .eq(TerminalPlaylist::getTerminalId, terminalId)
                        .orderByDesc(TerminalPlaylist::getStartTime)
        );
    }

    @Override
    public Page<TerminalHeartbeat> heartbeatLogs(Long terminalId, int page, int size) {
        Page<TerminalHeartbeat> p = new Page<>(page, size);
        terminalHeartbeatMapper.selectPage(p,
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<TerminalHeartbeat>()
                        .eq(TerminalHeartbeat::getTerminalId, terminalId)
                        .orderByDesc(TerminalHeartbeat::getCreatedAt));
        return p;
    }

    private long getOfflineSeconds() {
        try {
            String val = System.getProperty("app.terminal.offline-seconds");
            if (val != null) {
                return Long.parseLong(val);
            }
        } catch (Exception ignored) {
        }
        return 300L;
    }

    @Override
    public List<TerminalPlaybackDto> playbackForTerminal(String code) {
        Terminal terminal = terminalMapper.selectOne(new LambdaQueryWrapper<Terminal>().eq(Terminal::getCode, code));
        if (terminal == null) {
            return List.of();
        }
        LocalDateTime now = LocalDateTime.now();
        List<TerminalPlaylist> binds = terminalPlaylistMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<TerminalPlaylist>()
                        .eq(TerminalPlaylist::getTerminalId, terminal.getId())
                        .eq(TerminalPlaylist::getActive, true)
                        .and(w -> w.isNull(TerminalPlaylist::getStartTime).or().le(TerminalPlaylist::getStartTime, now))
                        .and(w -> w.isNull(TerminalPlaylist::getEndTime).or().ge(TerminalPlaylist::getEndTime, now))
                        .orderByDesc(TerminalPlaylist::getStartTime)
        );
        return binds.stream().map(b -> {
            Playlist p = playlistMapper.selectById(b.getPlaylistId());
            List<PlaylistItem> items = playlistItemMapper.selectList(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<PlaylistItem>()
                            .eq(PlaylistItem::getPlaylistId, b.getPlaylistId())
                            .orderByAsc(PlaylistItem::getSortOrder)
            );
            Layout layout = p != null && p.getLayoutId() != null ? layoutMapper.selectById(p.getLayoutId()) : null;
            TerminalPlaybackDto dto = new TerminalPlaybackDto();
            dto.setPlaylist(p);
            dto.setItems(items);
            dto.setLayout(layout);
            return dto;
        }).collect(Collectors.toList());
    }
}

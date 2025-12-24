package com.example.volunteer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.volunteer.dto.HeartbeatRequest;
import com.example.volunteer.dto.TerminalPlaylistBindRequest;
import com.example.volunteer.dto.TerminalRequest;
import com.example.volunteer.entity.Terminal;
import com.example.volunteer.entity.TerminalPlaylist;
import com.example.volunteer.mapper.TerminalMapper;
import com.example.volunteer.mapper.TerminalPlaylistMapper;
import com.example.volunteer.service.TerminalService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class TerminalServiceImpl implements TerminalService {

    private final TerminalMapper terminalMapper;
    private final TerminalPlaylistMapper terminalPlaylistMapper;

    public TerminalServiceImpl(TerminalMapper terminalMapper, TerminalPlaylistMapper terminalPlaylistMapper) {
        this.terminalMapper = terminalMapper;
        this.terminalPlaylistMapper = terminalPlaylistMapper;
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
            return t;
        }
        t.setStatus(request.getStatus());
        t.setLastHeartbeat(LocalDateTime.now());
        t.setUpdatedAt(LocalDateTime.now());
        terminalMapper.updateById(t);
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
}

package com.example.volunteer.service.impl;

import com.example.volunteer.dto.PlaylistItemDto;
import com.example.volunteer.dto.PlaylistRequest;
import com.example.volunteer.entity.Playlist;
import com.example.volunteer.entity.PlaylistItem;
import com.example.volunteer.entity.TerminalPlaylist;
import com.example.volunteer.mapper.PlaylistItemMapper;
import com.example.volunteer.mapper.PlaylistMapper;
import com.example.volunteer.mapper.TerminalPlaylistMapper;
import com.example.volunteer.service.PlaylistService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PlaylistServiceImpl implements PlaylistService {

    private final PlaylistMapper playlistMapper;
    private final PlaylistItemMapper playlistItemMapper;
    private final TerminalPlaylistMapper terminalPlaylistMapper;

    public PlaylistServiceImpl(PlaylistMapper playlistMapper, PlaylistItemMapper playlistItemMapper,
                               TerminalPlaylistMapper terminalPlaylistMapper) {
        this.playlistMapper = playlistMapper;
        this.playlistItemMapper = playlistItemMapper;
        this.terminalPlaylistMapper = terminalPlaylistMapper;
    }

    @Override
    @Transactional
    public Playlist create(PlaylistRequest request) {
        Playlist playlist = new Playlist();
        playlist.setName(request.getName());
        playlist.setDescription(request.getDescription());
        playlist.setCreatedAt(LocalDateTime.now());
        playlist.setUpdatedAt(LocalDateTime.now());
        playlistMapper.insert(playlist);

        saveItems(playlist.getId(), request.getItems());
        return playlist;
    }

    @Override
    @Transactional
    public Playlist update(Long id, PlaylistRequest request) {
        Playlist playlist = playlistMapper.selectById(id);
        if (playlist == null) {
            return null;
        }
        playlist.setName(request.getName());
        playlist.setDescription(request.getDescription());
        playlist.setUpdatedAt(LocalDateTime.now());
        playlistMapper.updateById(playlist);

        playlistItemMapper.delete(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<PlaylistItem>()
                .eq(PlaylistItem::getPlaylistId, id));
        saveItems(id, request.getItems());
        return playlist;
    }

    @Override
    public List<Playlist> list() {
        return playlistMapper.selectList(null);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        playlistMapper.deleteById(id);
        playlistItemMapper.delete(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<PlaylistItem>()
                        .eq(PlaylistItem::getPlaylistId, id)
        );
        // 清理终端绑定
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.example.volunteer.entity.TerminalPlaylist> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.example.volunteer.entity.TerminalPlaylist>()
                        .eq(com.example.volunteer.entity.TerminalPlaylist::getPlaylistId, id);
        terminalPlaylistMapper.delete(wrapper);
    }

    @Override
    public List<PlaylistItem> items(Long playlistId) {
        return playlistItemMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<PlaylistItem>()
                        .eq(PlaylistItem::getPlaylistId, playlistId)
                        .orderByAsc(PlaylistItem::getSortOrder)
        );
    }

    private void saveItems(Long playlistId, List<PlaylistItemDto> items) {
        if (items == null) {
            return;
        }
        for (PlaylistItemDto dto : items) {
            PlaylistItem item = new PlaylistItem();
            item.setPlaylistId(playlistId);
            item.setMediaId(dto.getMediaId());
            item.setContentId(dto.getContentId());
            item.setDisplayDuration(dto.getDisplayDuration());
            item.setSortOrder(dto.getSortOrder());
            playlistItemMapper.insert(item);
        }
    }
}

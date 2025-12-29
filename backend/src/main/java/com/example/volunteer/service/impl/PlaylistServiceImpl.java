package com.example.volunteer.service.impl;

import com.example.volunteer.dto.PlaylistItemDto;
import com.example.volunteer.dto.PlaylistPreviewDto;
import com.example.volunteer.dto.PlaylistRequest;
import com.example.volunteer.entity.ContentItem;
import com.example.volunteer.entity.Layout;
import com.example.volunteer.entity.MediaAsset;
import com.example.volunteer.entity.Playlist;
import com.example.volunteer.entity.PlaylistItem;
import com.example.volunteer.entity.TerminalPlaylist;
import com.example.volunteer.mapper.ContentItemMapper;
import com.example.volunteer.mapper.LayoutMapper;
import com.example.volunteer.mapper.MediaAssetMapper;
import com.example.volunteer.mapper.PlaylistItemMapper;
import com.example.volunteer.mapper.PlaylistMapper;
import com.example.volunteer.mapper.TerminalPlaylistMapper;
import com.example.volunteer.service.PlaylistService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PlaylistServiceImpl implements PlaylistService {

    private final PlaylistMapper playlistMapper;
    private final PlaylistItemMapper playlistItemMapper;
    private final TerminalPlaylistMapper terminalPlaylistMapper;
    private final LayoutMapper layoutMapper;
    private final MediaAssetMapper mediaAssetMapper;
    private final ContentItemMapper contentItemMapper;

    public PlaylistServiceImpl(PlaylistMapper playlistMapper, PlaylistItemMapper playlistItemMapper,
                               TerminalPlaylistMapper terminalPlaylistMapper,
                               LayoutMapper layoutMapper,
                               MediaAssetMapper mediaAssetMapper,
                               ContentItemMapper contentItemMapper) {
        this.playlistMapper = playlistMapper;
        this.playlistItemMapper = playlistItemMapper;
        this.terminalPlaylistMapper = terminalPlaylistMapper;
        this.layoutMapper = layoutMapper;
        this.mediaAssetMapper = mediaAssetMapper;
        this.contentItemMapper = contentItemMapper;
    }

    @Override
    @Transactional
    public Playlist create(PlaylistRequest request) {
        Playlist playlist = new Playlist();
        playlist.setName(request.getName());
        playlist.setDescription(request.getDescription());
        playlist.setCoverUrl(request.getCoverUrl());
        playlist.setLayoutId(request.getLayoutId());
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
        playlist.setCoverUrl(request.getCoverUrl());
        playlist.setLayoutId(request.getLayoutId());
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

    @Override
    public PlaylistPreviewDto preview(Long playlistId) {
        Playlist playlist = playlistMapper.selectById(playlistId);
        if (playlist == null) {
            return null;
        }
        List<PlaylistItem> items = items(playlistId);
        Layout layout = playlist.getLayoutId() != null ? layoutMapper.selectById(playlist.getLayoutId()) : null;
        
        // 加载媒体资源
        Set<Long> mediaIds = items.stream()
                .filter(i -> i.getMediaId() != null)
                .map(PlaylistItem::getMediaId)
                .collect(Collectors.toSet());
        List<MediaAsset> mediaAssets = mediaIds.isEmpty()
                ? List.of()
                : mediaAssetMapper.selectBatchIds(mediaIds);
        
        // 加载内容资源
        Set<Long> contentIds = items.stream()
                .filter(i -> i.getContentId() != null)
                .map(PlaylistItem::getContentId)
                .collect(Collectors.toSet());
        List<ContentItem> contentAssets = contentIds.isEmpty()
                ? List.of()
                : contentItemMapper.selectBatchIds(contentIds);

        PlaylistPreviewDto dto = new PlaylistPreviewDto();
        dto.setPlaylist(playlist);
        dto.setLayout(layout);
        dto.setItems(items);
        dto.setMediaAssets(mediaAssets);
        dto.setContentAssets(contentAssets);
        return dto;
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
            item.setAreaIndex(dto.getAreaIndex());
            playlistItemMapper.insert(item);
        }
    }
}

package com.example.volunteer.service.impl;

import com.example.volunteer.dto.PlaylistItemDto;
import com.example.volunteer.dto.PlaylistRequest;
import com.example.volunteer.entity.Playlist;
import com.example.volunteer.entity.PlaylistItem;
import com.example.volunteer.mapper.PlaylistItemMapper;
import com.example.volunteer.mapper.PlaylistMapper;
import com.example.volunteer.service.PlaylistService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PlaylistServiceImpl implements PlaylistService {

    private final PlaylistMapper playlistMapper;
    private final PlaylistItemMapper playlistItemMapper;

    public PlaylistServiceImpl(PlaylistMapper playlistMapper, PlaylistItemMapper playlistItemMapper) {
        this.playlistMapper = playlistMapper;
        this.playlistItemMapper = playlistItemMapper;
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

        if (request.getItems() != null) {
            for (PlaylistItemDto dto : request.getItems()) {
                PlaylistItem item = new PlaylistItem();
                item.setPlaylistId(playlist.getId());
                item.setMediaId(dto.getMediaId());
                item.setContentId(dto.getContentId());
                item.setDisplayDuration(dto.getDisplayDuration());
                item.setSortOrder(dto.getSortOrder());
                playlistItemMapper.insert(item);
            }
        }
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
    }
}

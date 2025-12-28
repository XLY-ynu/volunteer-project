package com.example.volunteer.dto;

import com.example.volunteer.entity.BroadcastJob;
import com.example.volunteer.entity.ContentItem;
import com.example.volunteer.entity.MediaAsset;
import lombok.Data;

@Data
public class BroadcastPlaybackDto {
    private BroadcastJob job;
    private MediaAsset media;
    private ContentItem content;
}

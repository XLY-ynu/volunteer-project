package com.example.volunteer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.volunteer.dto.BroadcastRequest;
import com.example.volunteer.entity.BroadcastJob;
import com.example.volunteer.mapper.BroadcastJobMapper;
import com.example.volunteer.service.BroadcastService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class BroadcastServiceImpl implements BroadcastService {

    private final BroadcastJobMapper broadcastJobMapper;

    public BroadcastServiceImpl(BroadcastJobMapper broadcastJobMapper) {
        this.broadcastJobMapper = broadcastJobMapper;
    }

    @Override
    public BroadcastJob create(BroadcastRequest request) {
        BroadcastJob job = new BroadcastJob();
        job.setTitle(request.getTitle());
        job.setMediaId(request.getMediaId());
        job.setContentId(request.getContentId());
        job.setTargetGroup(request.getTargetGroup());
        job.setTargetTerminalCode(request.getTargetTerminalCode());
        job.setStartTime(request.getStartTime());
        job.setEndTime(request.getEndTime());
        job.setStatus("scheduled");
        broadcastJobMapper.insert(job);
        return job;
    }

    @Override
    public Page<BroadcastJob> page(int page, int size, String targetGroup, String targetTerminalCode) {
        LambdaQueryWrapper<BroadcastJob> wrapper = new LambdaQueryWrapper<>();
        if (targetGroup != null && !targetGroup.isEmpty()) {
            wrapper.eq(BroadcastJob::getTargetGroup, targetGroup);
        }
        if (targetTerminalCode != null && !targetTerminalCode.isEmpty()) {
            wrapper.eq(BroadcastJob::getTargetTerminalCode, targetTerminalCode);
        }
        wrapper.orderByDesc(BroadcastJob::getStartTime);
        Page<BroadcastJob> p = new Page<>(page, size);
        broadcastJobMapper.selectPage(p, wrapper);
        return p;
    }

    @Override
    public Page<BroadcastJob> activeForTerminal(String terminalCode, int page, int size) {
        LocalDateTime now = LocalDateTime.now();
        LambdaQueryWrapper<BroadcastJob> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w.eq(BroadcastJob::getTargetTerminalCode, terminalCode)
                .or().isNull(BroadcastJob::getTargetTerminalCode));
        wrapper.and(w -> w.isNull(BroadcastJob::getStartTime).or().le(BroadcastJob::getStartTime, now));
        wrapper.and(w -> w.isNull(BroadcastJob::getEndTime).or().ge(BroadcastJob::getEndTime, now));
        wrapper.orderByDesc(BroadcastJob::getStartTime);
        Page<BroadcastJob> p = new Page<>(page, size);
        broadcastJobMapper.selectPage(p, wrapper);
        return p;
    }
}

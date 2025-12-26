package com.example.volunteer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.volunteer.dto.BroadcastRequest;
import com.example.volunteer.entity.BroadcastJob;
import com.example.volunteer.mapper.BroadcastJobMapper;
import com.example.volunteer.service.BroadcastService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        return activeForTerminal(terminalCode, null, page, size);
    }

    @Override
    public Page<BroadcastJob> activeForTerminal(String terminalCode, String groupName, int page, int size) {
        LocalDateTime now = LocalDateTime.now();
        
        // 获取所有未结束的插播（进行中 + 待执行）
        LambdaQueryWrapper<BroadcastJob> wrapper = new LambdaQueryWrapper<>();
        
        // 未结束：结束时间为空（永久）或结束时间在当前时间之后
        wrapper.and(w -> w.isNull(BroadcastJob::getEndTime).or().ge(BroadcastJob::getEndTime, now));
        wrapper.orderByDesc(BroadcastJob::getStartTime);
        
        Page<BroadcastJob> p = new Page<>(page, size);
        broadcastJobMapper.selectPage(p, wrapper);
        
        // 在内存中过滤匹配的插播
        List<BroadcastJob> filtered = p.getRecords().stream().filter(job -> {
            String jobTerminal = job.getTargetTerminalCode();
            String jobGroup = job.getTargetGroup();
            
            // 全部终端（两个字段都为空）
            boolean isAllTerminals = (jobTerminal == null || jobTerminal.isEmpty()) 
                    && (jobGroup == null || jobGroup.isEmpty());
            if (isAllTerminals) return true;
            
            // 指定该终端
            if (jobTerminal != null && !jobTerminal.isEmpty() && jobTerminal.equals(terminalCode)) {
                return true;
            }
            
            // 指定该分组
            if (groupName != null && !groupName.isEmpty() 
                    && jobGroup != null && !jobGroup.isEmpty() 
                    && jobGroup.equals(groupName)) {
                return true;
            }
            
            return false;
        }).collect(java.util.stream.Collectors.toList());
        
        p.setRecords(filtered);
        p.setTotal(filtered.size());
        return p;
    }

    @Override
    public void delete(Long id) {
        broadcastJobMapper.deleteById(id);
    }

    @Override
    public Map<String, Long> countByStatus() {
        LocalDateTime now = LocalDateTime.now();
        List<BroadcastJob> all = broadcastJobMapper.selectList(null);
        
        long active = 0, pending = 0, completed = 0;
        for (BroadcastJob job : all) {
            LocalDateTime start = job.getStartTime();
            LocalDateTime end = job.getEndTime();
            
            // 已结束
            if (end != null && end.isBefore(now)) {
                completed++;
            }
            // 未开始
            else if (start != null && start.isAfter(now)) {
                pending++;
            }
            // 进行中
            else {
                active++;
            }
        }
        
        Map<String, Long> result = new HashMap<>();
        result.put("active", active);
        result.put("pending", pending);
        result.put("completed", completed);
        return result;
    }
}

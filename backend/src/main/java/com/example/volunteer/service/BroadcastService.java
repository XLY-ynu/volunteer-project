package com.example.volunteer.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.volunteer.dto.BroadcastRequest;
import com.example.volunteer.entity.BroadcastJob;

public interface BroadcastService {
    BroadcastJob create(BroadcastRequest request);
    Page<BroadcastJob> page(int page, int size, String targetGroup, String targetTerminalCode);
    Page<BroadcastJob> activeForTerminal(String terminalCode, int page, int size);
    Page<BroadcastJob> activeForTerminal(String terminalCode, String groupName, int page, int size);
    void delete(Long id);
    java.util.Map<String, Long> countByStatus();
}

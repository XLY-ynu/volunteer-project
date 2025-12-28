package com.example.volunteer.schedule;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.volunteer.entity.NotificationLog;
import com.example.volunteer.mapper.NotificationLogMapper;
import com.example.volunteer.service.NotificationService;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class NotificationRetryScheduler {

    private final NotificationLogMapper notificationLogMapper;
    private final NotificationService notificationService;
    private final int batchSize;

    public NotificationRetryScheduler(NotificationLogMapper notificationLogMapper,
                                      NotificationService notificationService,
                                      @Value("${app.notification.retry-batch-size:50}") int batchSize) {
        this.notificationLogMapper = notificationLogMapper;
        this.notificationService = notificationService;
        this.batchSize = batchSize;
    }

    @Scheduled(fixedDelayString = "${app.notification.retry-scan-ms:60000}")
    public void retryFailed() {
        LocalDateTime now = LocalDateTime.now();
        QueryWrapper<NotificationLog> w = new QueryWrapper<>();
        w.eq("status", "failed")
                .le("next_retry_at", now)
                .last("limit " + batchSize);
        List<NotificationLog> logs = notificationLogMapper.selectList(w);
        for (NotificationLog log : logs) {
            notificationService.retry(log);
        }
    }
}

package com.example.volunteer.service;

import com.example.volunteer.entity.NotificationLog;

public interface NotificationService {
    NotificationLog send(String channel, String target, String title, String content);
    NotificationLog retry(NotificationLog log);
}

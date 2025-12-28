package com.example.volunteer.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.volunteer.common.ApiResponse;
import com.example.volunteer.dto.NotificationChannelConfigRequest;
import com.example.volunteer.dto.NotificationTestRequest;
import com.example.volunteer.entity.NotificationChannelConfig;
import com.example.volunteer.entity.NotificationLog;
import com.example.volunteer.mapper.NotificationChannelConfigMapper;
import com.example.volunteer.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/monitor")
public class NotificationConfigController {

    private final NotificationChannelConfigMapper configMapper;
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    public NotificationConfigController(NotificationChannelConfigMapper configMapper,
                                        NotificationService notificationService,
                                        ObjectMapper objectMapper) {
        this.configMapper = configMapper;
        this.notificationService = notificationService;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/notification-configs")
    public ApiResponse<List<NotificationChannelConfig>> listConfigs() {
        return ApiResponse.ok(configMapper.selectList(new LambdaQueryWrapper<>()));
    }

    @GetMapping("/notification-configs/{channel}")
    public ApiResponse<NotificationChannelConfig> getConfig(@PathVariable String channel) {
        NotificationChannelConfig config = configMapper.selectOne(
                new LambdaQueryWrapper<NotificationChannelConfig>().eq(NotificationChannelConfig::getChannel, channel));
        return ApiResponse.ok(config);
    }

    @PutMapping("/notification-configs/{channel}")
    public ApiResponse<NotificationChannelConfig> saveConfig(@PathVariable String channel,
                                                             @RequestBody NotificationChannelConfigRequest request) {
        NotificationChannelConfig config = configMapper.selectOne(
                new LambdaQueryWrapper<NotificationChannelConfig>().eq(NotificationChannelConfig::getChannel, channel));
        if (config == null) {
            config = new NotificationChannelConfig();
            config.setChannel(channel);
            config.setCreatedAt(LocalDateTime.now());
        }
        if (request.getEnabled() != null) {
            config.setEnabled(request.getEnabled());
        } else if (config.getEnabled() == null) {
            config.setEnabled(true);
        }
        if (request.getConfig() != null) {
            String error = validateConfig(channel, request.getConfig());
            if (error != null) {
                return ApiResponse.fail(error);
            }
            try {
                config.setConfigJson(objectMapper.writeValueAsString(request.getConfig()));
            } catch (Exception ex) {
                return ApiResponse.fail("配置格式错误");
            }
        }
        config.setUpdatedAt(LocalDateTime.now());
        if (config.getId() == null) {
            configMapper.insert(config);
        } else {
            configMapper.updateById(config);
        }
        return ApiResponse.ok(config);
    }

    @PostMapping("/notification-configs/{channel}/validate")
    public ApiResponse<Map<String, Object>> validateConfig(@PathVariable String channel,
                                                           @RequestBody(required = false) NotificationChannelConfigRequest request) {
        Map<String, Object> result = new HashMap<>();
        Map<String, Object> config = request != null ? request.getConfig() : null;
        if (config == null) {
            NotificationChannelConfig existing = configMapper.selectOne(
                    new LambdaQueryWrapper<NotificationChannelConfig>().eq(NotificationChannelConfig::getChannel, channel));
            if (existing != null && StringUtils.hasText(existing.getConfigJson())) {
                try {
                    config = objectMapper.readValue(existing.getConfigJson(), Map.class);
                } catch (Exception ex) {
                    config = Map.of();
                }
            }
        }
        String error = validateConfig(channel, config);
        result.put("valid", error == null);
        result.put("message", error == null ? "配置校验通过" : error);
        return ApiResponse.ok(result);
    }

    @PostMapping("/notification-test")
    public ApiResponse<NotificationLog> testSend(@Valid @RequestBody NotificationTestRequest request) {
        String title = StringUtils.hasText(request.getTitle()) ? request.getTitle() : "通知测试";
        String content = StringUtils.hasText(request.getContent()) ? request.getContent() : "这是一条测试消息";
        return ApiResponse.ok(notificationService.send(request.getChannel(), request.getTarget(), title, content));
    }

    private String validateConfig(String channel, Map<String, Object> config) {
        if (config == null) {
            return "配置不能为空";
        }
        if ("email".equalsIgnoreCase(channel)) {
            if (!StringUtils.hasText(asString(config.get("host"))) || !StringUtils.hasText(asString(config.get("username")))) {
                return "邮件配置缺少host或账号";
            }
            if (!StringUtils.hasText(asString(config.get("password")))) {
                return "邮件配置缺少密码";
            }
            return null;
        }
        if ("sms".equalsIgnoreCase(channel)) {
            if (!StringUtils.hasText(asString(config.get("endpoint")))) {
                return "短信配置缺少endpoint";
            }
            return null;
        }
        if ("wechat".equalsIgnoreCase(channel) || "dingtalk".equalsIgnoreCase(channel)) {
            if (!StringUtils.hasText(asString(config.get("webhookUrl"))) && !StringUtils.hasText(asString(config.get("webhook")))) {
                return "Webhook未配置";
            }
            return null;
        }
        return null;
    }

    private String asString(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}

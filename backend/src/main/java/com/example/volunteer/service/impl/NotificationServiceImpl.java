package com.example.volunteer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.volunteer.entity.NotificationChannelConfig;
import com.example.volunteer.entity.NotificationLog;
import com.example.volunteer.mapper.NotificationChannelConfigMapper;
import com.example.volunteer.mapper.NotificationLogMapper;
import com.example.volunteer.service.NotificationService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationLogMapper notificationLogMapper;
    private final NotificationChannelConfigMapper channelConfigMapper;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;
    private final int defaultMaxRetries;
    private final long retryDelaySeconds;

    public NotificationServiceImpl(NotificationLogMapper notificationLogMapper,
                                   NotificationChannelConfigMapper channelConfigMapper,
                                   ObjectMapper objectMapper,
                                   @Value("${app.notification.retry-max:3}") int defaultMaxRetries,
                                   @Value("${app.notification.retry-delay-seconds:120}") long retryDelaySeconds) {
        this.notificationLogMapper = notificationLogMapper;
        this.channelConfigMapper = channelConfigMapper;
        this.objectMapper = objectMapper;
        this.restTemplate = new RestTemplate();
        this.defaultMaxRetries = defaultMaxRetries;
        this.retryDelaySeconds = retryDelaySeconds;
    }

    @Override
    public NotificationLog send(String channel, String target, String title, String content) {
        NotificationLog log = new NotificationLog();
        log.setChannel(normalizeChannel(channel));
        log.setTarget(target);
        log.setTitle(title);
        log.setContent(content);
        log.setCreatedAt(LocalDateTime.now());
        log.setUpdatedAt(LocalDateTime.now());
        log.setRetryCount(0);
        log.setMaxRetries(defaultMaxRetries);
        try {
            SendResult result = dispatch(log.getChannel(), target, title, content);
            log.setStatus("sent");
            log.setProviderMessageId(result.messageId);
            log.setErrorMessage(null);
            log.setNextRetryAt(null);
        } catch (Exception ex) {
            log.setStatus("failed");
            log.setErrorMessage(ex.getMessage());
            log.setNextRetryAt(LocalDateTime.now().plusSeconds(retryDelaySeconds));
        }
        notificationLogMapper.insert(log);
        return log;
    }

    @Override
    public NotificationLog retry(NotificationLog log) {
        int retryCount = log.getRetryCount() == null ? 0 : log.getRetryCount();
        int maxRetries = log.getMaxRetries() == null ? defaultMaxRetries : log.getMaxRetries();
        if (retryCount >= maxRetries) {
            log.setStatus("abandoned");
            log.setUpdatedAt(LocalDateTime.now());
            notificationLogMapper.updateById(log);
            return log;
        }
        try {
            SendResult result = dispatch(normalizeChannel(log.getChannel()), log.getTarget(), log.getTitle(), log.getContent());
            log.setStatus("sent");
            log.setProviderMessageId(result.messageId);
            log.setErrorMessage(null);
            log.setNextRetryAt(null);
        } catch (Exception ex) {
            log.setStatus("failed");
            log.setRetryCount(retryCount + 1);
            log.setErrorMessage(ex.getMessage());
            log.setNextRetryAt(LocalDateTime.now().plusSeconds(retryDelaySeconds));
        }
        log.setUpdatedAt(LocalDateTime.now());
        notificationLogMapper.updateById(log);
        return log;
    }

    private SendResult dispatch(String channel, String target, String title, String content) {
        if ("web".equalsIgnoreCase(channel)) {
            return new SendResult(true, null);
        }
        NotificationChannelConfig config = channelConfigMapper.selectOne(
                new LambdaQueryWrapper<NotificationChannelConfig>().eq(NotificationChannelConfig::getChannel, channel));
        if (config == null || Boolean.FALSE.equals(config.getEnabled())) {
            throw new IllegalStateException("通知通道未启用");
        }
        Map<String, Object> cfg = parseConfig(config.getConfigJson());
        if ("email".equalsIgnoreCase(channel)) {
            return sendEmail(target, title, content, cfg);
        }
        if ("sms".equalsIgnoreCase(channel)) {
            return sendSms(target, title, content, cfg);
        }
        if ("wechat".equalsIgnoreCase(channel)) {
            return sendWechat(target, title, content, cfg);
        }
        if ("dingtalk".equalsIgnoreCase(channel)) {
            return sendDingtalk(target, title, content, cfg);
        }
        throw new IllegalArgumentException("未知通知通道");
    }

    private SendResult sendEmail(String target, String title, String content, Map<String, Object> cfg) {
        String host = asString(cfg.get("host"));
        String username = asString(cfg.get("username"));
        String password = asString(cfg.get("password"));
        String from = asString(cfg.get("from"));
        Integer port = asInt(cfg.get("port"), 25);
        boolean tls = asBool(cfg.get("tls"), false);
        if (!StringUtils.hasText(host) || !StringUtils.hasText(username)) {
            throw new IllegalStateException("邮件通道配置不完整");
        }
        if (!StringUtils.hasText(target)) {
            throw new IllegalStateException("邮件收件人为空");
        }
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(host);
        sender.setPort(port);
        sender.setUsername(username);
        sender.setPassword(password);
        Properties props = sender.getJavaMailProperties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", String.valueOf(tls));
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(StringUtils.hasText(from) ? from : username);
        message.setTo(target.split(","));
        message.setSubject(title);
        message.setText(content);
        sender.send(message);
        return new SendResult(true, null);
    }

    private SendResult sendWechat(String target, String title, String content, Map<String, Object> cfg) {
        String webhook = asString(cfg.get("webhookUrl"));
        if (!StringUtils.hasText(webhook)) {
            webhook = asString(cfg.get("webhook"));
        }
        if (!StringUtils.hasText(webhook)) {
            throw new IllegalStateException("企微Webhook未配置");
        }
        String payload = String.format("%s\n%s\n%s", title, content, StringUtils.hasText(target) ? "目标:" + target : "");
        Map<String, Object> body = Map.of(
                "msgtype", "text",
                "text", Map.of("content", payload)
        );
        restTemplate.postForEntity(webhook, body, String.class);
        return new SendResult(true, null);
    }

    private SendResult sendDingtalk(String target, String title, String content, Map<String, Object> cfg) {
        String webhook = asString(cfg.get("webhookUrl"));
        if (!StringUtils.hasText(webhook)) {
            webhook = asString(cfg.get("webhook"));
        }
        if (!StringUtils.hasText(webhook)) {
            throw new IllegalStateException("钉钉Webhook未配置");
        }
        String payload = String.format("%s\n%s\n%s", title, content, StringUtils.hasText(target) ? "目标:" + target : "");
        Map<String, Object> body = Map.of(
                "msgtype", "text",
                "text", Map.of("content", payload)
        );
        restTemplate.postForEntity(webhook, body, String.class);
        return new SendResult(true, null);
    }

    private SendResult sendSms(String target, String title, String content, Map<String, Object> cfg) {
        String endpoint = asString(cfg.get("endpoint"));
        if (!StringUtils.hasText(endpoint)) {
            throw new IllegalStateException("短信接口未配置");
        }
        if (!StringUtils.hasText(target)) {
            throw new IllegalStateException("短信目标为空");
        }
        String template = asString(cfg.get("payloadTemplate"));
        if (!StringUtils.hasText(template)) {
            template = "{\"phone\":\"{target}\",\"title\":\"{title}\",\"content\":\"{content}\"}";
        }
        String payload = template
                .replace("{target}", safe(target))
                .replace("{title}", safe(title))
                .replace("{content}", safe(content));
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        Object headerObj = cfg.get("headers");
        if (headerObj instanceof Map<?, ?> headerMap) {
            headerMap.forEach((k, v) -> headers.add(String.valueOf(k), v == null ? "" : String.valueOf(v)));
        }
        HttpEntity<String> entity = new HttpEntity<>(payload, headers);
        restTemplate.postForEntity(endpoint, entity, String.class);
        return new SendResult(true, null);
    }

    private Map<String, Object> parseConfig(String json) {
        if (!StringUtils.hasText(json)) {
            return Collections.emptyMap();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (Exception ex) {
            return Collections.emptyMap();
        }
    }

    private String normalizeChannel(String channel) {
        return StringUtils.hasText(channel) ? channel.toLowerCase() : "web";
    }

    private String asString(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private Integer asInt(Object value, int fallback) {
        if (value == null) return fallback;
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (Exception ex) {
            return fallback;
        }
    }

    private boolean asBool(Object value, boolean fallback) {
        if (value == null) return fallback;
        return "true".equalsIgnoreCase(String.valueOf(value)) || "1".equals(String.valueOf(value));
    }

    private String safe(String value) {
        return value == null ? "" : value.replace("\"", "\\\"");
    }

    private static class SendResult {
        private final boolean success;
        private final String messageId;

        private SendResult(boolean success, String messageId) {
            this.success = success;
            this.messageId = messageId;
        }
    }
}

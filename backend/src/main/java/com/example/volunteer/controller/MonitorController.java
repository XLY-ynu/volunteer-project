package com.example.volunteer.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.volunteer.common.ApiResponse;
import com.example.volunteer.dto.ActivityReminderLogDto;
import com.example.volunteer.entity.AlertSilence;
import com.example.volunteer.entity.AlertSubscription;
import com.example.volunteer.entity.Activity;
import com.example.volunteer.entity.ActivityReminderLog;
import com.example.volunteer.entity.NotificationLog;
import com.example.volunteer.entity.Terminal;
import com.example.volunteer.entity.TerminalAlertHistory;
import com.example.volunteer.entity.TerminalGroupRule;
import com.example.volunteer.entity.TerminalHeartbeat;
import com.example.volunteer.entity.Volunteer;
import com.example.volunteer.mapper.ActivityMapper;
import com.example.volunteer.mapper.ActivityReminderLogMapper;
import com.example.volunteer.mapper.MediaAssetMapper;
import com.example.volunteer.mapper.PlaylistMapper;
import com.example.volunteer.mapper.AlertSilenceMapper;
import com.example.volunteer.mapper.AlertSubscriptionMapper;
import com.example.volunteer.mapper.NotificationLogMapper;
import com.example.volunteer.mapper.TerminalGroupRuleMapper;
import com.example.volunteer.mapper.TerminalHeartbeatMapper;
import com.example.volunteer.mapper.TerminalAlertHistoryMapper;
import com.example.volunteer.mapper.TerminalMapper;
import com.example.volunteer.mapper.VolunteerMapper;
import com.example.volunteer.service.NotificationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.ArrayList;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/monitor")
public class MonitorController {

    private final TerminalMapper terminalMapper;
    private final TerminalHeartbeatMapper terminalHeartbeatMapper;
    private final TerminalGroupRuleMapper terminalGroupRuleMapper;
    private final TerminalAlertHistoryMapper terminalAlertHistoryMapper;
    private final NotificationLogMapper notificationLogMapper;
    private final NotificationService notificationService;
    private final AlertSubscriptionMapper alertSubscriptionMapper;
    private final AlertSilenceMapper alertSilenceMapper;
    private final MediaAssetMapper mediaAssetMapper;
    private final PlaylistMapper playlistMapper;
    private final ActivityMapper activityMapper;
    private final ActivityReminderLogMapper activityReminderLogMapper;
    private final VolunteerMapper volunteerMapper;
    private final long offlineSeconds;
    private final long alertIntervalSeconds;

    public MonitorController(TerminalMapper terminalMapper, MediaAssetMapper mediaAssetMapper,
                             PlaylistMapper playlistMapper, ActivityMapper activityMapper,
                             TerminalHeartbeatMapper terminalHeartbeatMapper,
                             TerminalGroupRuleMapper terminalGroupRuleMapper,
                             TerminalAlertHistoryMapper terminalAlertHistoryMapper,
                             NotificationLogMapper notificationLogMapper,
                             NotificationService notificationService,
                             AlertSubscriptionMapper alertSubscriptionMapper,
                             AlertSilenceMapper alertSilenceMapper,
                             ActivityReminderLogMapper activityReminderLogMapper,
                             VolunteerMapper volunteerMapper,
                             @Value("${app.terminal.offline-seconds:300}") long offlineSeconds,
                             @Value("${app.monitor.alert-interval-seconds:300}") long alertIntervalSeconds) {
        this.terminalMapper = terminalMapper;
        this.terminalHeartbeatMapper = terminalHeartbeatMapper;
        this.terminalGroupRuleMapper = terminalGroupRuleMapper;
        this.terminalAlertHistoryMapper = terminalAlertHistoryMapper;
        this.notificationLogMapper = notificationLogMapper;
        this.notificationService = notificationService;
        this.alertSubscriptionMapper = alertSubscriptionMapper;
        this.alertSilenceMapper = alertSilenceMapper;
        this.mediaAssetMapper = mediaAssetMapper;
        this.playlistMapper = playlistMapper;
        this.activityMapper = activityMapper;
        this.activityReminderLogMapper = activityReminderLogMapper;
        this.volunteerMapper = volunteerMapper;
        this.offlineSeconds = offlineSeconds;
        this.alertIntervalSeconds = alertIntervalSeconds;
    }

    @GetMapping("/summary")
    public ApiResponse<Map<String, Object>> summary() {
        Map<String, Object> map = new HashMap<>();
        map.put("terminalTotal", terminalMapper.selectCount(new QueryWrapper<>()));
        map.put("mediaTotal", mediaAssetMapper.selectCount(new QueryWrapper<>()));
        map.put("playlistTotal", playlistMapper.selectCount(new QueryWrapper<>()));
        map.put("activityTotal", activityMapper.selectCount(new QueryWrapper<>()));
        return ApiResponse.ok(map);
    }

    @GetMapping("/terminal-status")
    public ApiResponse<Map<String, Object>> terminalStatus() {
        List<Terminal> terminals = terminalMapper.selectList(new QueryWrapper<>());
        LocalDateTime threshold = LocalDateTime.now().minusSeconds(offlineSeconds);
        List<Terminal> offline = terminals.stream()
                .filter(t -> t.getLastHeartbeat() != null && t.getLastHeartbeat().isBefore(threshold))
                .collect(Collectors.toList());
        Map<String, Object> map = new HashMap<>();
        map.put("online", terminals.size() - offline.size());
        map.put("offline", offline.size());
        map.put("offlineTerminals", offline.stream().map(t -> {
            Map<String, Object> o = new HashMap<>();
            o.put("id", t.getId());
            o.put("name", t.getName());
            o.put("code", t.getCode());
            o.put("lastHeartbeat", t.getLastHeartbeat());
            return o;
        }).collect(Collectors.toList()));
        return ApiResponse.ok(map);
    }

    @GetMapping("/group-rules")
    public ApiResponse<List<TerminalGroupRule>> groupRules() {
        return ApiResponse.ok(terminalGroupRuleMapper.selectList(new QueryWrapper<>()));
    }

    @PostMapping("/group-rules")
    public ApiResponse<TerminalGroupRule> createRule(@RequestBody TerminalGroupRule rule) {
        if (!StringUtils.hasText(rule.getGroupName())) {
            return ApiResponse.fail("分组名称不能为空");
        }
        long count = terminalGroupRuleMapper.selectCount(new QueryWrapper<TerminalGroupRule>()
                .eq("group_name", rule.getGroupName()));
        if (count > 0) {
            return ApiResponse.fail("该分组规则已存在");
        }
        rule.setOfflineThreshold(rule.getOfflineThreshold() == null ? 1 : rule.getOfflineThreshold());
        rule.setEnabled(rule.getEnabled() == null ? Boolean.TRUE : rule.getEnabled());
        if (!StringUtils.hasText(rule.getNotifyChannel())) {
            rule.setNotifyChannel("web");
        }
        rule.setCreatedAt(LocalDateTime.now());
        rule.setUpdatedAt(LocalDateTime.now());
        terminalGroupRuleMapper.insert(rule);
        return ApiResponse.ok(rule);
    }

    @PutMapping("/group-rules/{id}")
    public ApiResponse<TerminalGroupRule> updateRule(@PathVariable Long id, @RequestBody TerminalGroupRule rule) {
        TerminalGroupRule existing = terminalGroupRuleMapper.selectById(id);
        if (existing == null) {
            return ApiResponse.fail("规则不存在");
        }
        if (StringUtils.hasText(rule.getGroupName())) {
            existing.setGroupName(rule.getGroupName());
        }
        if (rule.getOfflineThreshold() != null) {
            existing.setOfflineThreshold(rule.getOfflineThreshold());
        }
        if (rule.getEnabled() != null) {
            existing.setEnabled(rule.getEnabled());
        }
        if (rule.getNotifyChannel() != null) {
            existing.setNotifyChannel(rule.getNotifyChannel());
        }
        if (rule.getNotifyTarget() != null) {
            existing.setNotifyTarget(rule.getNotifyTarget());
        }
        existing.setUpdatedAt(LocalDateTime.now());
        terminalGroupRuleMapper.updateById(existing);
        return ApiResponse.ok(existing);
    }

    @DeleteMapping("/group-rules/{id}")
    public ApiResponse<Void> deleteRule(@PathVariable Long id) {
        terminalGroupRuleMapper.deleteById(id);
        return ApiResponse.ok(null);
    }

    @GetMapping("/group-alerts")
    public ApiResponse<List<Map<String, Object>>> groupAlerts() {
        List<Terminal> terminals = terminalMapper.selectList(new QueryWrapper<>());
        LocalDateTime threshold = LocalDateTime.now().minusSeconds(offlineSeconds);
        Map<String, List<Terminal>> grouped = terminals.stream()
                .collect(Collectors.groupingBy(t -> t.getGroupName() == null ? "未分组" : t.getGroupName()));
        Map<String, TerminalGroupRule> ruleMap = terminalGroupRuleMapper.selectList(new QueryWrapper<>()).stream()
                .collect(Collectors.toMap(TerminalGroupRule::getGroupName, r -> r, (a, b) -> a));
        List<AlertSubscription> subscriptions = alertSubscriptionMapper.selectList(new QueryWrapper<>());
        List<AlertSilence> silences = alertSilenceMapper.selectList(new QueryWrapper<>());
        List<Map<String, Object>> list = new ArrayList<>();
        for (Map.Entry<String, List<Terminal>> entry : grouped.entrySet()) {
            String group = entry.getKey();
            List<Terminal> items = entry.getValue();
            long offline = items.stream()
                    .filter(t -> t.getLastHeartbeat() != null && t.getLastHeartbeat().isBefore(threshold))
                    .count();
            TerminalGroupRule rule = ruleMap.get(group);
            Integer thresholdCount = rule != null ? rule.getOfflineThreshold() : null;
            boolean enabled = rule != null && Boolean.TRUE.equals(rule.getEnabled());
            boolean alert = enabled && thresholdCount != null && offline >= thresholdCount;
            boolean silenced = alert && isSilenced(group, rule != null ? rule.getNotifyChannel() : null, silences);
            Map<String, Object> map = new HashMap<>();
            map.put("groupName", group);
            map.put("total", items.size());
            map.put("offline", offline);
            map.put("ruleThreshold", thresholdCount);
            map.put("ruleEnabled", enabled);
            map.put("alert", alert);
            map.put("notifyChannel", rule != null ? rule.getNotifyChannel() : null);
            map.put("notifyTarget", rule != null ? rule.getNotifyTarget() : null);
            map.put("silenced", silenced);
            list.add(map);
            if (alert) {
                List<Recipient> recipients = buildRecipients(group, rule, subscriptions);
                if (recipients.isEmpty()) {
                    recipients.add(new Recipient("web", "admin"));
                }
                for (Recipient recipient : recipients) {
                    boolean recipientSilenced = isSilenced(group, recipient.channel, silences);
                    boolean canNotify = !recipientSilenced && shouldNotify(group, recipient);
                    if (canNotify) {
                        sendNotification(group, offline, thresholdCount, recipient);
                    }
                    if (canNotify || recipientSilenced) {
                        recordAlertHistory(group, (int) items.size(), (int) offline, thresholdCount, recipient, recipientSilenced);
                    }
                }
            }
        }
        return ApiResponse.ok(list);
    }

    @GetMapping("/offline-trend")
    public ApiResponse<List<Map<String, Object>>> offlineTrend(@RequestParam(defaultValue = "7") int days,
                                                               @RequestParam(required = false) String groupName) {
        LocalDateTime start = LocalDateTime.now().minusDays(days);
        QueryWrapper<TerminalHeartbeat> w = new QueryWrapper<>();
        w.ge("created_at", start);
        if (StringUtils.hasText(groupName)) {
            List<Terminal> terminals = terminalMapper.selectList(new QueryWrapper<Terminal>()
                    .eq("group_name", groupName));
            Set<Long> ids = terminals.stream().map(Terminal::getId).collect(Collectors.toSet());
            if (ids.isEmpty()) {
                return ApiResponse.ok(List.of());
            }
            w.in("terminal_id", ids);
        }
        w.select("DATE(created_at) as day", "SUM(CASE WHEN status = 'offline' THEN 1 ELSE 0 END) as offlineCount");
        w.groupBy("DATE(created_at)");
        w.orderByAsc("DATE(created_at)");
        List<Map<String, Object>> raw = terminalHeartbeatMapper.selectMaps(w);
        Map<String, Map<String, Object>> sorted = new LinkedHashMap<>();
        for (Map<String, Object> row : raw) {
            Object dayObj = row.get("day");
            String day = dayObj instanceof LocalDate ? dayObj.toString() : String.valueOf(dayObj);
            sorted.put(day, row);
        }
        List<Map<String, Object>> result = new ArrayList<>(sorted.values());
        return ApiResponse.ok(result);
    }

    @GetMapping("/alert-history")
    public ApiResponse<Page<TerminalAlertHistory>> alertHistory(@RequestParam(defaultValue = "1") int page,
                                                                @RequestParam(defaultValue = "20") int size,
                                                                @RequestParam(required = false) String groupName) {
        QueryWrapper<TerminalAlertHistory> w = new QueryWrapper<>();
        if (StringUtils.hasText(groupName)) {
            w.eq("group_name", groupName);
        }
        w.orderByDesc("created_at");
        Page<TerminalAlertHistory> p = new Page<>(page, size);
        terminalAlertHistoryMapper.selectPage(p, w);
        return ApiResponse.ok(p);
    }

    @GetMapping("/notification-logs")
    public ApiResponse<Page<NotificationLog>> notificationLogs(@RequestParam(defaultValue = "1") int page,
                                                               @RequestParam(defaultValue = "20") int size,
                                                               @RequestParam(required = false) String channel) {
        QueryWrapper<NotificationLog> w = new QueryWrapper<>();
        if (StringUtils.hasText(channel)) {
            w.eq("channel", channel);
        }
        w.orderByDesc("created_at");
        Page<NotificationLog> p = new Page<>(page, size);
        notificationLogMapper.selectPage(p, w);
        return ApiResponse.ok(p);
    }

    @PostMapping("/notification-logs/{id}/retry")
    public ApiResponse<NotificationLog> retryNotification(@PathVariable Long id) {
        NotificationLog log = notificationLogMapper.selectById(id);
        if (log == null) {
            return ApiResponse.fail("通知记录不存在");
        }
        return ApiResponse.ok(notificationService.retry(log));
    }

    @GetMapping("/reminder-logs")
    public ApiResponse<Page<ActivityReminderLogDto>> reminderLogs(@RequestParam(defaultValue = "1") int page,
                                                                  @RequestParam(defaultValue = "20") int size,
                                                                  @RequestParam(required = false) Long activityId,
                                                                  @RequestParam(required = false) String status,
                                                                  @RequestParam(required = false) String type) {
        QueryWrapper<ActivityReminderLog> w = new QueryWrapper<>();
        if (activityId != null) {
            w.eq("activity_id", activityId);
        }
        if (StringUtils.hasText(status)) {
            w.eq("status", status);
        }
        if (StringUtils.hasText(type)) {
            w.eq("reminder_type", type);
        }
        w.orderByDesc("created_at");
        Page<ActivityReminderLog> p = new Page<>(page, size);
        activityReminderLogMapper.selectPage(p, w);
        List<ActivityReminderLogDto> records = p.getRecords().stream().map(log -> {
            ActivityReminderLogDto dto = new ActivityReminderLogDto();
            dto.setId(log.getId());
            dto.setActivityId(log.getActivityId());
            dto.setVolunteerId(log.getVolunteerId());
            dto.setReminderType(log.getReminderType());
            dto.setChannel(log.getChannel());
            dto.setStatus(log.getStatus());
            dto.setMessage(log.getMessage());
            dto.setCreatedAt(log.getCreatedAt());
            Activity activity = activityMapper.selectById(log.getActivityId());
            Volunteer volunteer = volunteerMapper.selectById(log.getVolunteerId());
            dto.setActivityTitle(activity != null ? activity.getTitle() : null);
            dto.setVolunteerName(volunteer != null ? volunteer.getName() : null);
            dto.setVolunteerPhone(volunteer != null ? volunteer.getPhone() : null);
            return dto;
        }).collect(Collectors.toList());
        Page<ActivityReminderLogDto> result = new Page<>(page, size);
        result.setTotal(p.getTotal());
        result.setRecords(records);
        return ApiResponse.ok(result);
    }

    @GetMapping("/alert-subscriptions")
    public ApiResponse<List<AlertSubscription>> alertSubscriptions() {
        return ApiResponse.ok(alertSubscriptionMapper.selectList(new QueryWrapper<>()));
    }

    @PostMapping("/alert-subscriptions")
    public ApiResponse<AlertSubscription> createAlertSubscription(@RequestBody AlertSubscription subscription) {
        if (!StringUtils.hasText(subscription.getChannel())) {
            subscription.setChannel("web");
        }
        if (subscription.getEnabled() == null) {
            subscription.setEnabled(true);
        }
        subscription.setCreatedAt(LocalDateTime.now());
        subscription.setUpdatedAt(LocalDateTime.now());
        alertSubscriptionMapper.insert(subscription);
        return ApiResponse.ok(subscription);
    }

    @PutMapping("/alert-subscriptions/{id}")
    public ApiResponse<AlertSubscription> updateAlertSubscription(@PathVariable Long id, @RequestBody AlertSubscription subscription) {
        AlertSubscription existing = alertSubscriptionMapper.selectById(id);
        if (existing == null) {
            return ApiResponse.fail("订阅不存在");
        }
        if (subscription.getGroupName() != null) {
            existing.setGroupName(subscription.getGroupName());
        }
        if (subscription.getChannel() != null) {
            existing.setChannel(subscription.getChannel());
        }
        if (subscription.getTarget() != null) {
            existing.setTarget(subscription.getTarget());
        }
        if (subscription.getEnabled() != null) {
            existing.setEnabled(subscription.getEnabled());
        }
        existing.setUpdatedAt(LocalDateTime.now());
        alertSubscriptionMapper.updateById(existing);
        return ApiResponse.ok(existing);
    }

    @DeleteMapping("/alert-subscriptions/{id}")
    public ApiResponse<Void> deleteAlertSubscription(@PathVariable Long id) {
        alertSubscriptionMapper.deleteById(id);
        return ApiResponse.ok(null);
    }

    @GetMapping("/alert-silences")
    public ApiResponse<List<AlertSilence>> alertSilences() {
        return ApiResponse.ok(alertSilenceMapper.selectList(new QueryWrapper<>()));
    }

    @PostMapping("/alert-silences")
    public ApiResponse<AlertSilence> createAlertSilence(@RequestBody AlertSilence silence) {
        if (!StringUtils.hasText(silence.getChannel())) {
            silence.setChannel("web");
        }
        if (silence.getEnabled() == null) {
            silence.setEnabled(true);
        }
        silence.setCreatedAt(LocalDateTime.now());
        silence.setUpdatedAt(LocalDateTime.now());
        alertSilenceMapper.insert(silence);
        return ApiResponse.ok(silence);
    }

    @PutMapping("/alert-silences/{id}")
    public ApiResponse<AlertSilence> updateAlertSilence(@PathVariable Long id, @RequestBody AlertSilence silence) {
        AlertSilence existing = alertSilenceMapper.selectById(id);
        if (existing == null) {
            return ApiResponse.fail("静默配置不存在");
        }
        if (silence.getGroupName() != null) {
            existing.setGroupName(silence.getGroupName());
        }
        if (silence.getChannel() != null) {
            existing.setChannel(silence.getChannel());
        }
        if (silence.getStartTime() != null) {
            existing.setStartTime(silence.getStartTime());
        }
        if (silence.getEndTime() != null) {
            existing.setEndTime(silence.getEndTime());
        }
        if (silence.getEnabled() != null) {
            existing.setEnabled(silence.getEnabled());
        }
        existing.setUpdatedAt(LocalDateTime.now());
        alertSilenceMapper.updateById(existing);
        return ApiResponse.ok(existing);
    }

    @DeleteMapping("/alert-silences/{id}")
    public ApiResponse<Void> deleteAlertSilence(@PathVariable Long id) {
        alertSilenceMapper.deleteById(id);
        return ApiResponse.ok(null);
    }

    private boolean shouldNotify(String groupName, Recipient recipient) {
        LocalDateTime since = LocalDateTime.now().minusSeconds(alertIntervalSeconds);
        QueryWrapper<TerminalAlertHistory> w = new QueryWrapper<>();
        w.eq("group_name", groupName)
                .eq("channel", recipient.channel)
                .eq("target", recipient.target)
                .eq("silenced", false)
                .ge("created_at", since)
                .orderByDesc("created_at")
                .last("limit 1");
        return terminalAlertHistoryMapper.selectOne(w) == null;
    }

    private boolean isSilenced(String groupName, String channel, List<AlertSilence> silences) {
        if (silences == null || silences.isEmpty()) {
            return false;
        }
        String resolvedChannel = StringUtils.hasText(channel) ? channel : "web";
        LocalDateTime now = LocalDateTime.now();
        for (AlertSilence silence : silences) {
            if (!Boolean.TRUE.equals(silence.getEnabled())) {
                continue;
            }
            if (StringUtils.hasText(silence.getGroupName()) && !silence.getGroupName().equals(groupName)) {
                continue;
            }
            if (StringUtils.hasText(silence.getChannel()) && !silence.getChannel().equals(resolvedChannel)) {
                continue;
            }
            if (silence.getStartTime() != null && now.isBefore(silence.getStartTime())) {
                continue;
            }
            if (silence.getEndTime() != null && now.isAfter(silence.getEndTime())) {
                continue;
            }
            return true;
        }
        return false;
    }

    private List<Recipient> buildRecipients(String groupName, TerminalGroupRule rule, List<AlertSubscription> subscriptions) {
        List<Recipient> recipients = new ArrayList<>();
        if (rule != null && StringUtils.hasText(rule.getNotifyChannel())) {
            recipients.add(new Recipient(rule.getNotifyChannel(), rule.getNotifyTarget()));
        }
        if (subscriptions == null) {
            return recipients;
        }
        for (AlertSubscription sub : subscriptions) {
            if (!Boolean.TRUE.equals(sub.getEnabled())) {
                continue;
            }
            if (StringUtils.hasText(sub.getGroupName()) && !sub.getGroupName().equals(groupName)) {
                continue;
            }
            recipients.add(new Recipient(sub.getChannel(), sub.getTarget()));
        }
        return recipients;
    }

    private void recordAlertHistory(String groupName, int total, int offline, Integer threshold, Recipient recipient, boolean silenced) {
        TerminalAlertHistory history = new TerminalAlertHistory();
        history.setGroupName(groupName);
        history.setTotal(total);
        history.setOffline(offline);
        history.setRuleThreshold(threshold);
        history.setChannel(recipient.channel);
        history.setTarget(recipient.target);
        history.setSilenced(silenced);
        history.setCreatedAt(LocalDateTime.now());
        terminalAlertHistoryMapper.insert(history);
    }

    private void sendNotification(String groupName, long offline, Integer threshold, Recipient recipient) {
        String title = "分组离线告警";
        String content = String.format("%s 离线 %d 台，超过阈值 %s", groupName, offline, threshold == null ? "-" : threshold);
        notificationService.send(recipient.channel, recipient.target, title, content);
    }

    private static class Recipient {
        private final String channel;
        private final String target;

        private Recipient(String channel, String target) {
            this.channel = StringUtils.hasText(channel) ? channel : "web";
            this.target = target;
        }
    }
}

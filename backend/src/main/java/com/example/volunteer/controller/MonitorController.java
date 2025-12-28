package com.example.volunteer.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.volunteer.common.ApiResponse;
import com.example.volunteer.entity.Terminal;
import com.example.volunteer.entity.TerminalGroupRule;
import com.example.volunteer.entity.TerminalHeartbeat;
import com.example.volunteer.mapper.ActivityMapper;
import com.example.volunteer.mapper.MediaAssetMapper;
import com.example.volunteer.mapper.PlaylistMapper;
import com.example.volunteer.mapper.TerminalGroupRuleMapper;
import com.example.volunteer.mapper.TerminalHeartbeatMapper;
import com.example.volunteer.mapper.TerminalMapper;
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
    private final MediaAssetMapper mediaAssetMapper;
    private final PlaylistMapper playlistMapper;
    private final ActivityMapper activityMapper;
    private final long offlineSeconds;

    public MonitorController(TerminalMapper terminalMapper, MediaAssetMapper mediaAssetMapper,
                             PlaylistMapper playlistMapper, ActivityMapper activityMapper,
                             TerminalHeartbeatMapper terminalHeartbeatMapper,
                             TerminalGroupRuleMapper terminalGroupRuleMapper,
                             @Value("${app.terminal.offline-seconds:300}") long offlineSeconds) {
        this.terminalMapper = terminalMapper;
        this.terminalHeartbeatMapper = terminalHeartbeatMapper;
        this.terminalGroupRuleMapper = terminalGroupRuleMapper;
        this.mediaAssetMapper = mediaAssetMapper;
        this.playlistMapper = playlistMapper;
        this.activityMapper = activityMapper;
        this.offlineSeconds = offlineSeconds;
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
            Map<String, Object> map = new HashMap<>();
            map.put("groupName", group);
            map.put("total", items.size());
            map.put("offline", offline);
            map.put("ruleThreshold", thresholdCount);
            map.put("ruleEnabled", enabled);
            map.put("alert", alert);
            map.put("notifyChannel", rule != null ? rule.getNotifyChannel() : null);
            map.put("notifyTarget", rule != null ? rule.getNotifyTarget() : null);
            list.add(map);
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
}

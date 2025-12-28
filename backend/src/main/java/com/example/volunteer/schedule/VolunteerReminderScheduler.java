package com.example.volunteer.schedule;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.volunteer.entity.Activity;
import com.example.volunteer.entity.ActivityReminderLog;
import com.example.volunteer.entity.ActivitySignup;
import com.example.volunteer.entity.NotificationLog;
import com.example.volunteer.entity.Volunteer;
import com.example.volunteer.entity.VolunteerReminderSetting;
import com.example.volunteer.mapper.ActivityMapper;
import com.example.volunteer.mapper.ActivityReminderLogMapper;
import com.example.volunteer.mapper.ActivitySignupMapper;
import com.example.volunteer.mapper.VolunteerMapper;
import com.example.volunteer.mapper.VolunteerReminderSettingMapper;
import com.example.volunteer.service.NotificationService;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class VolunteerReminderScheduler {

    private final ActivityMapper activityMapper;
    private final ActivitySignupMapper activitySignupMapper;
    private final VolunteerMapper volunteerMapper;
    private final VolunteerReminderSettingMapper reminderSettingMapper;
    private final ActivityReminderLogMapper reminderLogMapper;
    private final NotificationService notificationService;
    private final int defaultReminderMinutes;

    public VolunteerReminderScheduler(ActivityMapper activityMapper,
                                      ActivitySignupMapper activitySignupMapper,
                                      VolunteerMapper volunteerMapper,
                                      VolunteerReminderSettingMapper reminderSettingMapper,
                                      ActivityReminderLogMapper reminderLogMapper,
                                      NotificationService notificationService,
                                      @Value("${app.reminder.default-minutes:30}") int defaultReminderMinutes) {
        this.activityMapper = activityMapper;
        this.activitySignupMapper = activitySignupMapper;
        this.volunteerMapper = volunteerMapper;
        this.reminderSettingMapper = reminderSettingMapper;
        this.reminderLogMapper = reminderLogMapper;
        this.notificationService = notificationService;
        this.defaultReminderMinutes = defaultReminderMinutes;
    }

    @Scheduled(fixedDelayString = "${app.reminder.scan-ms:60000}")
    public void sendCheckinReminders() {
        List<ActivitySignup> signups = activitySignupMapper.selectList(
                new LambdaQueryWrapper<ActivitySignup>().eq(ActivitySignup::getStatus, "applied"));
        LocalDateTime now = LocalDateTime.now();
        for (ActivitySignup signup : signups) {
            Activity activity = activityMapper.selectById(signup.getActivityId());
            if (activity == null || activity.getStartTime() == null) {
                continue;
            }
            long minutesLeft = Duration.between(now, activity.getStartTime()).toMinutes();
            if (minutesLeft < 0) {
                continue;
            }
            Volunteer volunteer = volunteerMapper.selectById(signup.getVolunteerId());
            if (volunteer == null || !"approved".equals(volunteer.getStatus())) {
                continue;
            }
            VolunteerReminderSetting setting = getOrCreateSetting(volunteer);
            if (!Boolean.TRUE.equals(setting.getCheckinReminder())) {
                continue;
            }
            int reminderMinutes = setting.getReminderMinutes() == null ? defaultReminderMinutes : setting.getReminderMinutes();
            if (minutesLeft > reminderMinutes) {
                continue;
            }
            boolean alreadySent = reminderLogMapper.selectCount(new LambdaQueryWrapper<ActivityReminderLog>()
                    .eq(ActivityReminderLog::getVolunteerId, volunteer.getId())
                    .eq(ActivityReminderLog::getActivityId, activity.getId())
                    .eq(ActivityReminderLog::getReminderType, "checkin")) > 0;
            if (alreadySent) {
                continue;
            }
            String channel = setting.getChannel();
            String target = resolveTarget(channel, volunteer);
            if (!StringUtils.hasText(target) && !"web".equalsIgnoreCase(channel)) {
                continue;
            }
            String message = String.format("活动即将开始：%s，地点：%s，开始时间：%s，签到码：%s",
                    activity.getTitle(),
                    activity.getLocation() == null ? "-" : activity.getLocation(),
                    activity.getStartTime(),
                    activity.getCheckinCode() == null ? "-" : activity.getCheckinCode());
            NotificationLog log = notificationService.send(channel, target, "活动签到提醒", message);
            ActivityReminderLog reminderLog = new ActivityReminderLog();
            reminderLog.setVolunteerId(volunteer.getId());
            reminderLog.setActivityId(activity.getId());
            reminderLog.setReminderType("checkin");
            reminderLog.setChannel(channel);
            reminderLog.setStatus(log.getStatus());
            reminderLog.setMessage(message);
            reminderLog.setCreatedAt(LocalDateTime.now());
            reminderLogMapper.insert(reminderLog);
        }
    }

    private VolunteerReminderSetting getOrCreateSetting(Volunteer volunteer) {
        VolunteerReminderSetting setting = reminderSettingMapper.selectOne(
                new LambdaQueryWrapper<VolunteerReminderSetting>().eq(VolunteerReminderSetting::getVolunteerId, volunteer.getId()));
        if (setting != null) {
            return setting;
        }
        setting = new VolunteerReminderSetting();
        setting.setVolunteerId(volunteer.getId());
        setting.setSignupReminder(true);
        setting.setCheckinReminder(true);
        if (StringUtils.hasText(volunteer.getPhone())) {
            setting.setChannel("sms");
        } else if (StringUtils.hasText(volunteer.getEmail())) {
            setting.setChannel("email");
        } else {
            setting.setChannel("web");
        }
        setting.setReminderMinutes(defaultReminderMinutes);
        setting.setCreatedAt(LocalDateTime.now());
        setting.setUpdatedAt(LocalDateTime.now());
        reminderSettingMapper.insert(setting);
        return setting;
    }

    private String resolveTarget(String channel, Volunteer volunteer) {
        if ("web".equalsIgnoreCase(channel)) {
            return "";
        }
        if ("email".equalsIgnoreCase(channel)) {
            return volunteer.getEmail();
        }
        if ("sms".equalsIgnoreCase(channel)) {
            return volunteer.getPhone();
        }
        return volunteer.getPhone();
    }
}

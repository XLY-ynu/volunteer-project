/**
 * @Author: 孔令超
 * @Module: 活动参与 - 签到功能
 * @Description: 公开签到接口，支持通过签到码进行活动签到
 */
package com.example.volunteer.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.volunteer.common.ApiResponse;
import com.example.volunteer.entity.Activity;
import com.example.volunteer.entity.ActivitySignup;
import com.example.volunteer.entity.Volunteer;
import com.example.volunteer.mapper.ActivityMapper;
import com.example.volunteer.mapper.ActivitySignupMapper;
import com.example.volunteer.mapper.VolunteerMapper;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 公开签到控制器 - 无需登录即可签到
 */
@RestController
@RequestMapping("/api/public/activities")
public class ActivityCheckinController {

    private final ActivityMapper activityMapper;
    private final ActivitySignupMapper activitySignupMapper;
    private final VolunteerMapper volunteerMapper;

    public ActivityCheckinController(ActivityMapper activityMapper,
                                     ActivitySignupMapper activitySignupMapper,
                                     VolunteerMapper volunteerMapper) {
        this.activityMapper = activityMapper;
        this.activitySignupMapper = activitySignupMapper;
        this.volunteerMapper = volunteerMapper;
    }

    /**
     * 公开签到接口
     * 通过签到码、姓名、手机号进行签到
     */
    @PostMapping("/checkin")
    public ApiResponse<Void> checkin(@RequestBody Map<String, String> request) {
        String checkinCode = request.get("checkinCode");
        String name = request.get("name");
        String phone = request.get("phone");

        // 参数校验
        if (checkinCode == null || checkinCode.trim().isEmpty()) {
            return ApiResponse.fail("请输入签到码");
        }
        if (name == null || name.trim().isEmpty()) {
            return ApiResponse.fail("请输入姓名");
        }
        if (phone == null || phone.trim().isEmpty()) {
            return ApiResponse.fail("请输入手机号");
        }

        // 查找活动
        Activity activity = activityMapper.selectOne(new LambdaQueryWrapper<Activity>()
                .eq(Activity::getCheckinCode, checkinCode.trim()));
        if (activity == null) {
            return ApiResponse.fail("签到码无效");
        }

        // 查找志愿者
        Volunteer volunteer = volunteerMapper.selectOne(new LambdaQueryWrapper<Volunteer>()
                .eq(Volunteer::getPhone, phone.trim())
                .eq(Volunteer::getName, name.trim()));
        if (volunteer == null) {
            return ApiResponse.fail("未找到匹配的志愿者信息，请确认姓名和手机号");
        }

        // 检查志愿者状态
        if (!"approved".equals(volunteer.getStatus())) {
            return ApiResponse.fail("您的志愿者身份尚未审核通过");
        }

        // 查找报名记录
        ActivitySignup signup = activitySignupMapper.selectOne(new LambdaQueryWrapper<ActivitySignup>()
                .eq(ActivitySignup::getActivityId, activity.getId())
                .eq(ActivitySignup::getVolunteerId, volunteer.getId()));

        if (signup == null) {
            // 未报名，自动报名并签到
            signup = new ActivitySignup();
            signup.setActivityId(activity.getId());
            signup.setVolunteerId(volunteer.getId());
            signup.setStatus("checked_in");
            signup.setCheckinTime(LocalDateTime.now());
            signup.setCreatedAt(LocalDateTime.now());
            activitySignupMapper.insert(signup);
        } else if ("checked_in".equals(signup.getStatus())) {
            return ApiResponse.fail("您已签到过此活动");
        } else {
            // 更新签到状态
            signup.setStatus("checked_in");
            signup.setCheckinTime(LocalDateTime.now());
            activitySignupMapper.updateById(signup);
        }

        return ApiResponse.ok(null);
    }
}

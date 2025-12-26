package com.example.volunteer.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.volunteer.entity.MenuCategory;
import com.example.volunteer.entity.Role;
import com.example.volunteer.entity.Terminal;
import com.example.volunteer.entity.TerminalHeartbeat;
import com.example.volunteer.entity.User;
import com.example.volunteer.mapper.MenuCategoryMapper;
import com.example.volunteer.mapper.RoleMapper;
import com.example.volunteer.mapper.TerminalMapper;
import com.example.volunteer.mapper.TerminalHeartbeatMapper;
import com.example.volunteer.mapper.UserMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initAdmin(UserMapper userMapper, MenuCategoryMapper menuCategoryMapper, 
                                       TerminalMapper terminalMapper, TerminalHeartbeatMapper terminalHeartbeatMapper,
                                       RoleMapper roleMapper,
                                       PasswordEncoder passwordEncoder) {
        return args -> {
            // 初始化角色
            String[][] roles = {
                    {"ADMIN", "管理员", "系统管理员，可访问管理后台"},
                    {"VOLUNTEER", "志愿者", "志愿者用户，可访问志愿者端"}
            };
            for (String[] r : roles) {
                long count = roleMapper.selectCount(new LambdaQueryWrapper<Role>().eq(Role::getCode, r[0]));
                if (count == 0) {
                    Role role = new Role();
                    role.setCode(r[0]);
                    role.setName(r[1]);
                    role.setDescription(r[2]);
                    roleMapper.insert(role);
                }
            }
            
            // 初始化管理员用户
            User existing = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, "admin"));
            if (existing == null) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setNickname("管理员");
                admin.setRoleCode("ADMIN");
                admin.setEnabled(true);
                admin.setCreatedAt(LocalDateTime.now());
                admin.setUpdatedAt(LocalDateTime.now());
                userMapper.insert(admin);
            }
            
            // seed six main categories if not present
            String[][] categories = {
                    {"文明XX", "wenming"},
                    {"XX志愿者APP", "app"},
                    {"XX志愿者网", "web"},
                    {"雷锋热线", "leifeng"},
                    {"公益活动", "gongyi"},
                    {"公益广告", "ad"}
            };
            for (String[] c : categories) {
                long count = menuCategoryMapper.selectCount(new LambdaQueryWrapper<MenuCategory>()
                        .eq(MenuCategory::getCode, c[1]));
                if (count == 0) {
                    MenuCategory mc = new MenuCategory();
                    mc.setName(c[0]);
                    mc.setCode(c[1]);
                    mc.setSortOrder(0);
                    menuCategoryMapper.insert(mc);
                }
            }
            
            // 为没有心跳记录的终端创建初始心跳记录，并同步更新终端状态
            List<Terminal> terminals = terminalMapper.selectList(null);
            LocalDateTime offlineThreshold = LocalDateTime.now().minusSeconds(300); // 5分钟无心跳视为离线，与后端配置一致
            for (Terminal t : terminals) {
                LocalDateTime lastHb = t.getLastHeartbeat();
                String correctStatus = (lastHb != null && lastHb.isAfter(offlineThreshold)) ? "online" : "offline";
                
                // 更新终端表的状态
                if (!correctStatus.equals(t.getStatus())) {
                    t.setStatus(correctStatus);
                    t.setUpdatedAt(LocalDateTime.now());
                    terminalMapper.updateById(t);
                }
                
                // 处理心跳记录
                long heartbeatCount = terminalHeartbeatMapper.selectCount(
                        new LambdaQueryWrapper<TerminalHeartbeat>().eq(TerminalHeartbeat::getTerminalId, t.getId()));
                
                if (heartbeatCount == 0) {
                    // 创建新记录
                    TerminalHeartbeat hb = new TerminalHeartbeat();
                    hb.setTerminalId(t.getId());
                    hb.setStatus(correctStatus);
                    hb.setCreatedAt(lastHb != null ? lastHb : LocalDateTime.now());
                    terminalHeartbeatMapper.insert(hb);
                } else {
                    // 更新最新一条记录的状态
                    TerminalHeartbeat latestHb = terminalHeartbeatMapper.selectOne(
                            new LambdaQueryWrapper<TerminalHeartbeat>()
                                    .eq(TerminalHeartbeat::getTerminalId, t.getId())
                                    .orderByDesc(TerminalHeartbeat::getCreatedAt)
                                    .last("LIMIT 1"));
                    if (latestHb != null && !correctStatus.equals(latestHb.getStatus())) {
                        latestHb.setStatus(correctStatus);
                        terminalHeartbeatMapper.updateById(latestHb);
                    }
                }
            }
        };
    }
}

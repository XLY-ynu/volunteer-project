package com.example.volunteer.interceptor;

import com.example.volunteer.entity.OperationLog;
import com.example.volunteer.mapper.OperationLogMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.LocalDateTime;

@Component
public class OperationLogInterceptor implements HandlerInterceptor {

    private final OperationLogMapper operationLogMapper;

    public OperationLogInterceptor(OperationLogMapper operationLogMapper) {
        this.operationLogMapper = operationLogMapper;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                 Object handler, Exception ex) {
        try {
            OperationLog log = new OperationLog();
            
            // 获取当前用户
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal()))
                    ? auth.getName() : "anonymous";
            
            log.setUsername(username);
            log.setMethod(request.getMethod());
            log.setPath(request.getRequestURI());
            log.setStatus(response.getStatus());
            log.setCreatedAt(LocalDateTime.now());
            
            operationLogMapper.insert(log);
        } catch (Exception ignored) {
            // 日志记录失败不影响主流程
        }
    }
}

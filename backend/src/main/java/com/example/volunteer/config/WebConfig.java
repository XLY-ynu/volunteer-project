package com.example.volunteer.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.example.volunteer.interceptor.OperationLogInterceptor;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private static final Logger log = LoggerFactory.getLogger(WebConfig.class);

    @Value("${app.storage.root:uploads}")
    private String storageRoot;
    private final OperationLogInterceptor operationLogInterceptor;
    
    private Path resolvedUploadPath;

    public WebConfig(OperationLogInterceptor operationLogInterceptor) {
        this.operationLogInterceptor = operationLogInterceptor;
    }

    @PostConstruct
    public void initStorageDir() {
        // 智能检测 uploads 目录位置
        // 优先级: 1. 当前目录/uploads  2. backend/uploads  3. 创建当前目录/uploads
        Path currentDir = Paths.get("").toAbsolutePath();
        Path uploadsInCurrent = currentDir.resolve(storageRoot);
        Path uploadsInBackend = currentDir.resolve("backend").resolve(storageRoot);
        
        if (Files.exists(uploadsInCurrent) && Files.isDirectory(uploadsInCurrent)) {
            resolvedUploadPath = uploadsInCurrent;
            log.info("使用上传目录: {}", resolvedUploadPath);
        } else if (Files.exists(uploadsInBackend) && Files.isDirectory(uploadsInBackend)) {
            resolvedUploadPath = uploadsInBackend;
            log.info("使用上传目录 (backend子目录): {}", resolvedUploadPath);
        } else {
            // 默认在当前目录创建
            resolvedUploadPath = uploadsInCurrent;
            try {
                Files.createDirectories(resolvedUploadPath);
                log.info("创建上传目录: {}", resolvedUploadPath);
            } catch (Exception e) {
                log.warn("创建上传目录失败: {}", e.getMessage());
            }
        }
    }
    
    public Path getResolvedUploadPath() {
        return resolvedUploadPath;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(false);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 使用智能检测到的上传目录
        Path uploadPath = resolvedUploadPath != null ? resolvedUploadPath : Paths.get(storageRoot).toAbsolutePath();
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadPath.toString() + "/");
        log.info("静态资源映射: /uploads/** -> {}", uploadPath);
    }

    @Override
    public void addInterceptors(org.springframework.web.servlet.config.annotation.InterceptorRegistry registry) {
        registry.addInterceptor(operationLogInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/ping", "/api/auth/**");
    }
}

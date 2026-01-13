package com.example.volunteer.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.example.volunteer.interceptor.OperationLogInterceptor;

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
    
    private volatile Path resolvedUploadPath;
    private final Object pathLock = new Object();

    public WebConfig(OperationLogInterceptor operationLogInterceptor) {
        this.operationLogInterceptor = operationLogInterceptor;
    }

    /**
     * 懒加载方式解析上传目录路径
     * 智能检测 uploads 目录位置
     * 优先级: 1. 当前目录/uploads  2. backend/uploads  3. 创建当前目录/uploads
     */
    private Path resolveUploadPath() {
        if (resolvedUploadPath != null) {
            return resolvedUploadPath;
        }
        synchronized (pathLock) {
            if (resolvedUploadPath != null) {
                return resolvedUploadPath;
            }
            
            String root = storageRoot != null ? storageRoot : "uploads";
            Path currentDir = Paths.get("").toAbsolutePath();
            Path uploadsInCurrent = currentDir.resolve(root);
            Path uploadsInBackend = currentDir.resolve("backend").resolve(root);
            
            log.info("当前工作目录: {}", currentDir);
            log.info("检查路径1: {} 存在={}", uploadsInCurrent, Files.exists(uploadsInCurrent));
            log.info("检查路径2: {} 存在={}", uploadsInBackend, Files.exists(uploadsInBackend));
            
            if (Files.exists(uploadsInCurrent) && Files.isDirectory(uploadsInCurrent)) {
                resolvedUploadPath = uploadsInCurrent;
                log.info("使用上传目录: {}", resolvedUploadPath);
            } else if (Files.exists(uploadsInBackend) && Files.isDirectory(uploadsInBackend)) {
                resolvedUploadPath = uploadsInBackend;
                log.info("使用上传目录 (backend子目录): {}", resolvedUploadPath);
            } else {
                // 默认在backend/uploads创建（更合理的默认位置）
                resolvedUploadPath = uploadsInBackend;
                try {
                    Files.createDirectories(resolvedUploadPath);
                    log.info("创建上传目录: {}", resolvedUploadPath);
                } catch (Exception e) {
                    log.warn("创建上传目录失败: {}", e.getMessage());
                    // 回退到当前目录
                    resolvedUploadPath = uploadsInCurrent;
                    try {
                        Files.createDirectories(resolvedUploadPath);
                    } catch (Exception e2) {
                        log.error("创建备用上传目录也失败: {}", e2.getMessage());
                    }
                }
            }
            return resolvedUploadPath;
        }
    }
    
    public Path getResolvedUploadPath() {
        return resolveUploadPath();
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
        // 使用懒加载方式获取上传目录
        Path uploadPath = resolveUploadPath();
        // Windows 路径需要转换为 file: URL 格式
        String resourceLocation = "file:///" + uploadPath.toString().replace("\\", "/") + "/";
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(resourceLocation);
        log.info("静态资源映射: /uploads/** -> {}", resourceLocation);
    }

    @Override
    public void addInterceptors(org.springframework.web.servlet.config.annotation.InterceptorRegistry registry) {
        registry.addInterceptor(operationLogInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/ping", "/api/auth/**");
    }
}

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
     * 优先级: 1. backend/uploads (有内容)  2. 当前目录/uploads (有内容)  3. backend/uploads (存在)  4. 当前目录/uploads
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
            log.info("检查路径1 (当前目录): {} 存在={}", uploadsInCurrent, Files.exists(uploadsInCurrent));
            log.info("检查路径2 (backend): {} 存在={}", uploadsInBackend, Files.exists(uploadsInBackend));
            
            // 检查哪个目录有实际内容（非空目录）
            boolean backendHasContent = hasContent(uploadsInBackend);
            boolean currentHasContent = hasContent(uploadsInCurrent);
            
            log.info("backend/uploads 有内容: {}, 当前目录/uploads 有内容: {}", backendHasContent, currentHasContent);
            
            // 优先使用有内容的 backend/uploads
            if (backendHasContent) {
                resolvedUploadPath = uploadsInBackend;
                log.info("使用上传目录 (backend有内容): {}", resolvedUploadPath);
            } else if (currentHasContent) {
                resolvedUploadPath = uploadsInCurrent;
                log.info("使用上传目录 (当前目录有内容): {}", resolvedUploadPath);
            } else if (Files.exists(uploadsInBackend) && Files.isDirectory(uploadsInBackend)) {
                // 都没内容，优先使用 backend/uploads
                resolvedUploadPath = uploadsInBackend;
                log.info("使用上传目录 (backend存在): {}", resolvedUploadPath);
            } else if (Files.exists(uploadsInCurrent) && Files.isDirectory(uploadsInCurrent)) {
                resolvedUploadPath = uploadsInCurrent;
                log.info("使用上传目录 (当前目录存在): {}", resolvedUploadPath);
            } else {
                // 都不存在，在backend/uploads创建
                resolvedUploadPath = uploadsInBackend;
                try {
                    Files.createDirectories(resolvedUploadPath);
                    log.info("创建上传目录: {}", resolvedUploadPath);
                } catch (Exception e) {
                    log.warn("创建上传目录失败: {}", e.getMessage());
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
    
    /**
     * 检查目录是否有内容（非空）
     */
    private boolean hasContent(Path dir) {
        if (!Files.exists(dir) || !Files.isDirectory(dir)) {
            return false;
        }
        try {
            return Files.list(dir).findFirst().isPresent();
        } catch (Exception e) {
            return false;
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

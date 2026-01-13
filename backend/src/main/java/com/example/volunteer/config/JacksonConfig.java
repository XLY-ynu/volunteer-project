package com.example.volunteer.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Configuration
public class JacksonConfig {

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        
        // 忽略未知属性
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        
        // 序列化时使用带空格的格式
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        
        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(outputFormatter) {
            @Override
            protected LocalDateTime _fromString(com.fasterxml.jackson.core.JsonParser p, 
                    com.fasterxml.jackson.databind.DeserializationContext ctxt, String string) {
                if (string == null || string.isEmpty()) {
                    return null;
                }
                // 尝试多种格式解析
                try {
                    return LocalDateTime.parse(string, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                } catch (DateTimeParseException e1) {
                    try {
                        return LocalDateTime.parse(string, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
                    } catch (DateTimeParseException e2) {
                        return LocalDateTime.parse(string, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                    }
                }
            }
        });
        
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(outputFormatter));
        
        mapper.registerModule(javaTimeModule);
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        return mapper;
    }
}

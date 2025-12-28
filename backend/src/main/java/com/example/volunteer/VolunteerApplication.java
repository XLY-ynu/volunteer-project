package com.example.volunteer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.mybatis.spring.annotation.MapperScan;

import jakarta.annotation.PostConstruct;
import java.util.TimeZone;

@SpringBootApplication
@EnableScheduling
@MapperScan("com.example.volunteer.mapper")
public class VolunteerApplication {
    
    @PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));
    }
    
    public static void main(String[] args) {
        SpringApplication.run(VolunteerApplication.class, args);
    }
}

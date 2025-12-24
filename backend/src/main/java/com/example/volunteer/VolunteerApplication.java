package com.example.volunteer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan("com.example.volunteer.mapper")
public class VolunteerApplication {
    public static void main(String[] args) {
        SpringApplication.run(VolunteerApplication.class, args);
    }
}

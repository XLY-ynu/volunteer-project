package com.example.volunteer.config;

import com.example.volunteer.security.JwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(request -> {
                var config = new org.springframework.web.cors.CorsConfiguration();
                config.setAllowedOrigins(java.util.List.of("*"));
                config.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                config.setAllowedHeaders(java.util.List.of("*"));
                return config;
            }))
            .csrf(csrf -> csrf.disable())
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // 公开接口
                .requestMatchers("/api/ping", "/api/auth/**").permitAll()
                .requestMatchers("/api/portal/auth/**").permitAll()
                .requestMatchers("/api/org/login").permitAll()
                .requestMatchers("/api/user-portal/register", "/api/user-portal/login", "/api/user-portal/orgs/**").permitAll()
                .requestMatchers("/uploads/**").permitAll()
                .requestMatchers("/api/public/**").permitAll()
                // 志愿者端（VOLUNTEER 和 USER 都可访问）
                .requestMatchers("/api/portal/**").hasAnyAuthority("VOLUNTEER", "USER")
                // 普通用户端
                .requestMatchers("/api/user-portal/**").hasAnyAuthority("USER", "VOLUNTEER")
                // 志愿者组织端
                .requestMatchers("/api/org/**").hasAuthority("ORG")
                // 管理员端
                .requestMatchers("/api/monitor/terminal-status").hasAnyAuthority("ADMIN", "ORG")
                .requestMatchers("/api/monitor/**").hasAuthority("ADMIN")
                .requestMatchers("/api/broadcasts/**").hasAnyAuthority("ADMIN", "ORG")
                .requestMatchers("/api/users/**", "/api/ops/**").hasAuthority("ADMIN")
                .requestMatchers("/api/activities/**", "/api/volunteers/**").hasAnyAuthority("ADMIN", "ORG")
                .requestMatchers("/api/layouts/**", "/api/layout-templates/**", "/api/playlists/**").hasAnyAuthority("ADMIN", "ORG")
                .requestMatchers("/api/media/**", "/api/categories/**", "/api/content/**", "/api/terminals/**").hasAnyAuthority("ADMIN", "ORG")
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}

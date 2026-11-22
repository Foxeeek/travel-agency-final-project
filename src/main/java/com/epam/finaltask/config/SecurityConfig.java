package com.epam.finaltask.config;

import com.epam.finaltask.dto.ApiResponse;
import com.epam.finaltask.token.JwtAuthenticationFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    private final ObjectMapper objectMapper;

    public SecurityConfig(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        return http.csrf(csrf -> csrf.disable())
                .logout(logout -> logout.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**", "/login")
                        .permitAll()
                        .anyRequest()
                        .authenticated()

                )
                .exceptionHandling(e ->
                        e.authenticationEntryPoint(
                                        (req, res, ex) -> {
                                            if (req.getRequestURI().startsWith("/api/")) {
                                                writeError(res, HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Authentication required");
                                            } else {
                                                res.sendRedirect("/login");
                                            }
                                        })

                                .accessDeniedHandler(

                                        (req, res, ex) ->
                                                writeError(res, HttpStatus.FORBIDDEN, "FORBIDDEN", "Access denied"))
                )
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }


    private void writeError(HttpServletResponse response, HttpStatus status, String code, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType("application/json");
        objectMapper.writeValue(response.getOutputStream(), new ApiResponse<>(code, message, null));
    }

}

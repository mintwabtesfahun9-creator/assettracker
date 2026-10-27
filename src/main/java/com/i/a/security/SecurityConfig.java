package com.i.a.security;

import com.i.a.dto.ApiErrorResponse;
import tools.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import java.time.Instant;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http, ObjectMapper objectMapper) throws Exception {

        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session ->
                session.sessionCreationPolicy(
                    SessionCreationPolicy.STATELESS))
            .exceptionHandling(exceptions -> exceptions
                .accessDeniedHandler((request, response, exception) -> {
                    response.setStatus(HttpStatus.FORBIDDEN.value());
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    objectMapper.writeValue(response.getOutputStream(),
                        new ApiErrorResponse(
                            Instant.now(),
                            HttpStatus.FORBIDDEN.value(),
                            "Access is denied",
                            request.getRequestURI()));
                }))
            .authorizeHttpRequests(auth -> auth

                .requestMatchers(
                    "/swagger-ui/**", "/v3/api-docs/**"
                ).permitAll()

                .requestMatchers(
                    HttpMethod.POST, "/api/auth/register"
                ).permitAll()

                .requestMatchers(
                    HttpMethod.GET, "/api/categories"
                ).hasAnyRole("ADMIN", "STAFF")

                .requestMatchers(
                    HttpMethod.POST, "/api/categories"
                ).hasRole("ADMIN")

                .requestMatchers(
                    HttpMethod.GET, "/api/assets/**"
                ).hasAnyRole("ADMIN", "STAFF")

                .requestMatchers(
                    HttpMethod.POST, "/api/assets"
                ).hasRole("ADMIN")

                .requestMatchers(
                    HttpMethod.PATCH, "/api/assets/**"
                ).hasRole("ADMIN")

                .requestMatchers(
                    HttpMethod.DELETE, "/api/assets/**"
                ).hasRole("ADMIN")

                .anyRequest().authenticated()
            )
            .httpBasic(basic -> {});

        return http.build();
    }
}

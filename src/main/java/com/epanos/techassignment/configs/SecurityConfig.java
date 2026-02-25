package com.epanos.techassignment.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Configures HTTP security for all endpoints.
     * <p>
     * All API endpoints require HTTP Basic Authentication.
     * Swagger UI and OpenAPI docs are publicly accessible so they can be browsed without credentials.
     * The application is stateless — no HTTP session is created or used.
     * CSRF is disabled as this is a stateless REST API.
     * </p>
     *
     * @param http the {@link HttpSecurity} to configure
     * @return the configured {@link SecurityFilterChain}
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        http
                // disable CSRF — not needed for stateless REST APIs
                .csrf(AbstractHttpConfigurer::disable)

                // no HTTP session — each request must carry credentials
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth
                        // allow Swagger UI and OpenAPI docs without authentication
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/actuator/health",
                                "/actuator/info"
                        ).permitAll()
                        // everything else requires authentication
                        .anyRequest().authenticated()
                )

                // enable HTTP Basic Auth
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }
}


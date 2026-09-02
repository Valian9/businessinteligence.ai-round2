package com.decisionlense.backend.controller;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SecurityConfig {

    // ============================================================
    // PASSWORD ENCODER
    // ============================================================

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // ============================================================
    // DEMO USERS
    // ============================================================

    @Bean
    public UserDetailsService userDetailsService(
            PasswordEncoder passwordEncoder) {

        UserDetails supplyChainManager =
                User.builder()
                        .username("supply")
                        .password(
                                passwordEncoder.encode("supply123")
                        )
                        .roles("SUPPLY_CHAIN_MANAGER")
                        .build();

        UserDetails marketingManager =
                User.builder()
                        .username("marketing")
                        .password(
                                passwordEncoder.encode("marketing123")
                        )
                        .roles("MARKETING_MANAGER")
                        .build();

        UserDetails executive =
                User.builder()
                        .username("executive")
                        .password(
                                passwordEncoder.encode("executive123")
                        )
                        .roles("EXECUTIVE")
                        .build();

        return new InMemoryUserDetailsManager(
                supplyChainManager,
                marketingManager,
                executive
        );
    }

    // ============================================================
    // SECURITY FILTER CHAIN
    // ============================================================

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http
                // ------------------------------------------------
                // CSRF
                // ------------------------------------------------
                .csrf(csrf -> csrf.disable())

                // ------------------------------------------------
                // CORS
                // ------------------------------------------------
                .cors(Customizer.withDefaults())

                // ------------------------------------------------
                // AUTHORIZATION
                // ------------------------------------------------
                .authorizeHttpRequests(auth -> auth

                        // Health endpoint can remain public
                        .requestMatchers(
                                "/api/health",
                                "/error"
                        ).permitAll()

                        .requestMatchers(
                                "/api/feedback/**",
                                "/api/telemetry/**"
                        ).hasAnyRole(
                                "SUPPLY_CHAIN_MANAGER",
                                "MARKETING_MANAGER",
                                "EXECUTIVE"
                        )

                        // ----------------------------------------
                        // MULTI-SOURCE ANALYSIS
                        // ----------------------------------------

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/analysis/multi-source"
                        )
                        .hasAnyRole(
                                "SUPPLY_CHAIN_MANAGER",
                                "MARKETING_MANAGER",
                                "EXECUTIVE"
                        )

                        // ----------------------------------------
                        // SINGLE SOURCE CSV ANALYSIS
                        // ----------------------------------------

                        .requestMatchers(
                                "/api/csv/**"
                        )
                        .authenticated()

                        // ----------------------------------------
                        // ALL OTHER API ENDPOINTS
                        // ----------------------------------------

                        .requestMatchers(
                                "/api/**"
                        )
                        .authenticated()

                        // ----------------------------------------
                        // EVERYTHING ELSE
                        // ----------------------------------------

                        .anyRequest()
                        .authenticated()
                )

                // ------------------------------------------------
                // HTTP BASIC
                // ------------------------------------------------
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }

    // ============================================================
    // CORS
    // ============================================================

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration =
                new CorsConfiguration();

        configuration.setAllowedOrigins(
                List.of(
                        "http://localhost:5500",
                        "http://127.0.0.1:5500"
                )
        );

        configuration.setAllowedMethods(
                List.of(
                        "GET",
                        "POST",
                        "PUT",
                        "DELETE",
                        "OPTIONS"
                )
        );

        configuration.setAllowedHeaders(
                List.of(
                        "Authorization",
                        "Content-Type",
                        "Accept"
                )
        );

        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration(
                "/**",
                configuration
        );

        return source;
    }
}
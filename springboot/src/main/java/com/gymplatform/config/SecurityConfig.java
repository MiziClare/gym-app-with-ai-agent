package com.gymplatform.config;

import com.gymplatform.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SecurityConfig {
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    UserDetailsService userDetailsService(UserMapper userMapper) {
        return username -> {
            var user = userMapper.findByUsername(username);
            if (user == null) {
                throw new org.springframework.security.core.userdetails.UsernameNotFoundException(username);
            }
            return org.springframework.security.core.userdetails.User
                    .withUsername(user.username())
                    .password(user.passwordHash())
                    .roles(user.role().name())
                    .disabled(!user.active())
                    .build();
        };
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        var csrf = CookieCsrfTokenRepository.withHttpOnlyFalse();
        csrf.setCookiePath("/");

        http
                .cors(cors -> {})
                .csrf(config -> config.csrfTokenRepository(csrf))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, "/api/auth/session", "/api/courses/**", "/api/sessions/**",
                                "/api/notices", "/api/coaches", "/api/equipment").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/session", "/api/auth/register").permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/coach/**").hasRole("COACH")
                        .requestMatchers("/api/equipment-reservations/**", "/api/coach-appointments/**")
                                .hasRole("MEMBER")
                        .requestMatchers("/api/membership/**").hasRole("MEMBER")
                        .requestMatchers("/api/messages/**", "/api/posts/**").hasAnyRole("MEMBER", "COACH")
                        .requestMatchers("/api/bookings/**", "/api/assistant/**").hasRole("MEMBER")
                        .anyRequest().authenticated());

        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource(@Value("${app.cors-origin}") String origin) {
        var config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(origin));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Content-Type", "X-XSRF-TOKEN"));
        config.setAllowCredentials(true);

        var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }
}

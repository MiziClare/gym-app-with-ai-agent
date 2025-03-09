package com.example.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * Cross-domain configuration
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.addAllowedOrigin("http://10.184.197.243:8080"); // Set the allowed origin address
        corsConfiguration.addAllowedOrigin("http://localhost:8080"); // Set the allowed origin address
        corsConfiguration.setAllowCredentials(true);  // Allow credentials
        corsConfiguration.addAllowedHeader("*"); // Set the allowed origin request header
        corsConfiguration.addAllowedMethod("*"); // Set the allowed origin request method
        source.registerCorsConfiguration("/**", corsConfiguration); // Register the cors configuration
        return new CorsFilter(source);
    }
}
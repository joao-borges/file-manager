package br.com.joaoborges.filemanager.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web Security Configuration
 *
 * Configures CORS (Cross-Origin Resource Sharing) policies for the application.
 * Replaces the insecure @CrossOrigin(origins = "*") annotations with proper
 * environment-specific CORS configuration.
 *
 * Security:
 * - Restricts allowed origins to configured domains
 * - Limits allowed HTTP methods
 * - Configures credential support
 * - Sets appropriate cache duration
 *
 * Configuration:
 * - Uses application.properties for environment-specific origins
 * - Supports multiple origins (comma-separated)
 * - Defaults to localhost for development
 */
@Configuration
public class WebSecurityConfig {

    /**
     * Allowed origins from configuration
     * Format: comma-separated list of origins
     * Example: http://localhost:3000,https://app.example.com
     */
    @Value("${cors.allowed-origins:http://localhost:3000,http://localhost:8080}")
    private String[] allowedOrigins;

    /**
     * Configure CORS mappings
     *
     * @return WebMvcConfigurer with CORS configuration
     */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                    // Allow only configured origins (not "*")
                    .allowedOrigins(allowedOrigins)

                    // Allow specific HTTP methods
                    .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")

                    // Allow specific headers
                    .allowedHeaders(
                        "Content-Type",
                        "Authorization",
                        "X-Requested-With",
                        "Accept",
                        "Origin"
                    )

                    // Allow credentials (cookies, authorization headers)
                    .allowCredentials(true)

                    // Cache preflight response for 1 hour
                    .maxAge(3600);

                // Allow WebSocket endpoint with CORS
                registry.addMapping("/ws/**")
                    .allowedOrigins(allowedOrigins)
                    .allowedMethods("GET", "POST")
                    .allowCredentials(true);
            }
        };
    }
}

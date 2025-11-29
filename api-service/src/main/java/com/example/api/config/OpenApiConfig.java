package com.example.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI/Swagger configuration for the SMTP Service REST API
 * 
 * This configuration provides comprehensive API documentation with:
 * - API metadata (title, description, version, contact information)
 * - JWT Bearer token authentication scheme
 * - Server information for different environments
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        // Define the security scheme for JWT
        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .name("Authorization")
                .description("JWT authentication token. Obtain from /api/login endpoint.");

        // Define security requirement
        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList("BearerAuth");

        return new OpenAPI()
                .info(new Info()
                        .title("SMTP Service REST API")
                        .version("1.0.0")
                        .description(
                                """
                                        Comprehensive REST API for the SMTP Email Service.

                                        This API provides complete email management functionality including:
                                        - User authentication and authorization with JWT tokens
                                        - Email sending, receiving, and management
                                        - Folder organization for email storage
                                        - User settings and preferences
                                        - Administrative operations for system management

                                        **Authentication**: Most endpoints require JWT Bearer token authentication.
                                        Use the /api/login endpoint to obtain a token, then click 'Authorize' and enter: Bearer <your-token>
                                        """)
                        .contact(new Contact()
                                .name("SMTP Service Team")
                                .email("support@devprabhakar.in")
                                .url("https://devprabhakar.in"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Local Development Server"),
                        new Server()
                                .url("https://api.devprabhakar.in")
                                .description("Production Server")))
                // Add security scheme to components
                .schemaRequirement("BearerAuth", securityScheme)
                // Apply security globally (can be overridden per endpoint)
                .addSecurityItem(securityRequirement);
    }
}

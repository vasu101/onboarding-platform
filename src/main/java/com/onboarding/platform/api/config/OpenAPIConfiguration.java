package com.onboarding.platform.api.config;

import io.micronaut.context.annotation.Factory;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;

/**
 * OpenAPI/Swagger configuration
 */
@OpenAPIDefinition(
        info = @Info(
                title = "Onboarding Platform API",
                version = "1.0.0",
                description = "Enterprise onboarding platform with workflow management, verification, and role-based access control",
                contact = @Contact(
                        name = "Onboarding Platform Team",
                        email = "support@onboarding.com"
                )
        ),
        servers = {
                @Server(
                        url = "http://localhost:8080",
                        description = "Development server"
                )
        },
        security = {
                @SecurityRequirement(name = "bearerAuth")
        }
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "JWT authentication token. Obtain from /api/auth/login endpoint."
)
@Factory
public class OpenAPIConfiguration {
}

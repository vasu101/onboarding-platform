package com.onboarding.platform.api.controller;

import com.onboarding.platform.api.dto.auth.AuthResponse;
import com.onboarding.platform.api.dto.auth.LoginRequest;
import com.onboarding.platform.api.dto.auth.RegisterRequest;
import com.onboarding.platform.security.jwt.JwtTokenGenerator;
import com.onboarding.platform.security.model.User;
import com.onboarding.platform.security.service.AuthenticationService;
import io.micronaut.context.annotation.Value;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Authentication endpoints
 */
@Controller("/api/auth")
@ExecuteOn(TaskExecutors.IO)
@Tag(name = "Authentication", description = "User authentication and registration endpoints")
public class AuthController {

    private static final Logger LOG = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationService authenticationService;
    private final JwtTokenGenerator jwtTokenGenerator;

    @Value("${jwt.expiration:3600}")
    private long tokenExpiration;

    public AuthController(JwtTokenGenerator jwtTokenGenerator, AuthenticationService authenticationService) {
        this.jwtTokenGenerator = jwtTokenGenerator;
        this.authenticationService = authenticationService;
    }

    @Post("/register")
    @Operation(
            summary = "Register new user",
            description = "Create a new user account with specified role"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "User registered successfully",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input or username/email already exists"
            )
    })
    public HttpResponse<AuthResponse> register(@Valid @Body RegisterRequest request) {
        LOG.info("Registration request for username: {}", request.getUsername());

        try {
            User user = authenticationService.register(
                    request.getUsername(),
                    request.getEmail(),
                    request.getPassword(),
                    request.getFullName(),
                    request.getRole()
            );

            String token = jwtTokenGenerator.generateToken(user);

            AuthResponse response = new AuthResponse(
                    token,
                    user.getId(),
                    user.getUsername(),
                    user.getEmail(),
                    user.getFullName(),
                    user.getRole(),
                    tokenExpiration
            );

            return HttpResponse.created(response);
        } catch (IllegalArgumentException e) {
            LOG.warn("Registration failed: {}", e.getMessage());
            return HttpResponse.badRequest();
        }
    }

    @Post("/login")
    @Operation(
            summary = "User login",
            description = "Authenticate user and return JWT token"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Login successful",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Invalid credentials"
            )
    })
    public HttpResponse<AuthResponse> login(@Valid @Body LoginRequest request) {
        LOG.info("Login request for username: {}", request.getUsername());

        Optional<User> userOpt = authenticationService.authenticate(request.getUsername(), request.getPassword());

        if(userOpt.isEmpty()) {
            LOG.warn("Login failed for username: {}", request.getUsername());
            return HttpResponse.unauthorized();
        }

        User user = userOpt.get();
        String token = jwtTokenGenerator.generateToken(user);

        AuthResponse response = new AuthResponse(
                token,
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.getRole(),
                tokenExpiration
        );

        return HttpResponse.ok(response);
    }
}

package com.onboarding.platform.api.controller;

import com.onboarding.platform.api.dto.AuthResponse;
import com.onboarding.platform.api.dto.LoginRequest;
import com.onboarding.platform.api.dto.RegisterRequest;
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
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Authentication endpoints
 */
@Controller("/api/auth")
@ExecuteOn(TaskExecutors.IO)
public class AuthController {

    private static final Logger LOG = LoggerFactory.getLogger(AuthController.class);

    private AuthenticationService authenticationService;
    private final JwtTokenGenerator jwtTokenGenerator;

    @Value("${jwt.expiration:3600}")
    private long tokenExpiration;

    public AuthController(JwtTokenGenerator jwtTokenGenerator, AuthenticationService authenticationService) {
        this.jwtTokenGenerator = jwtTokenGenerator;
        this.authenticationService = authenticationService;
    }

    @Post("/register")
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

package com.onboarding.platform.security.filter;

import com.onboarding.platform.security.jwt.JwtTokenGenerator;
import com.onboarding.platform.security.model.User;
import com.onboarding.platform.security.service.AuthenticationService;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.Filter;
import io.micronaut.http.filter.HttpServerFilter;
import io.micronaut.http.filter.ServerFilterChain;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.Optional;

/**
 * Filter to validate JWT tokens and set authentication context
 */
@Filter("/api/**")
public class JwtAuthenticationFilter implements HttpServerFilter {

    private static final Logger LOG = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenGenerator jwtTokenGenerator;
    private final AuthenticationService authenticationService;

    public JwtAuthenticationFilter(JwtTokenGenerator jwtTokenGenerator, AuthenticationService authenticationService) {
        this.jwtTokenGenerator = jwtTokenGenerator;
        this.authenticationService = authenticationService;
    }

    @Override
    public Publisher<MutableHttpResponse<?>> doFilter(HttpRequest<?> request, ServerFilterChain chain) {
        String path = request.getPath();

        if(isPublicEndpoint(path)) {
            LOG.debug("Public endpoint accessed: {}", path);
            return chain.proceed(request);
        }

        Optional<String> authHeader = request.getHeaders().get(AUTHORIZATION_HEADER, String.class);

        if(authHeader.isEmpty() || !authHeader.get().startsWith(BEARER_PREFIX)) {
            LOG.warn("Missing or invalid Authorization header for: {}", path);
            return Mono.just(HttpResponse.unauthorized());
        }

        String token = authHeader.get().substring(BEARER_PREFIX.length());

        if(!jwtTokenGenerator.validateToken(token)) {
            LOG.warn("Invalid JWT token for: {}", path);
            return Mono.just(HttpResponse.unauthorized());
        }

        String username = jwtTokenGenerator.extractUsername(token);
        if(username == null) {
            LOG.warn("Could not extract username from token");
            return Mono.just(HttpResponse.unauthorized());
        }

        Optional<User> userOpt = authenticationService.findByUsername(username);
        if(userOpt.isEmpty()) {
            LOG.warn("User not found: {}", username);
            return Mono.just(HttpResponse.unauthorized());
        }

        User user = userOpt.get();

        if(!user.getActive()) {
            LOG.warn("Inactive user attempted access: {}", username);
            return Mono.just(HttpResponse.unauthorized());
        }

        request.setAttribute("currentUser", user);

        LOG.debug("User authenticated: {} ({})", username, user.getRole());

        return chain.proceed(request);
    }

    private boolean isPublicEndpoint(String path) {
        return path.startsWith("/api/auth/") ||
                path.equals("/api/health") ||
                path.equals("/api/swagger") ||
                path.startsWith("/swagger");
    }
}

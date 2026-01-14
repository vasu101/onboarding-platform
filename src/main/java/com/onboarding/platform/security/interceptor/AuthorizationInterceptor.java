package com.onboarding.platform.security.interceptor;

import com.onboarding.platform.security.annotation.RequiresRole;
import com.onboarding.platform.security.model.User;
import com.onboarding.platform.security.model.UserRole;
import io.micronaut.aop.InterceptorBean;
import io.micronaut.aop.MethodInterceptor;
import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.context.ServerRequestContext;
import io.micronaut.http.exceptions.HttpStatusException;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Optional;

/**
 * Interceptor for role-based authorization
 */
@Singleton
@InterceptorBean(RequiresRole.class)
public class AuthorizationInterceptor implements MethodInterceptor<Object, Object> {

    private static final Logger LOG = LoggerFactory.getLogger(AuthorizationInterceptor.class);

    @Override
    public Object intercept(MethodInvocationContext<Object, Object> context) {
        AnnotationMetadata metadata = context.getAnnotationMetadata();

        UserRole[] requiredRoles = metadata.getValue(RequiresRole.class, UserRole[].class).orElse(null);

        if(requiredRoles == null) {
            return context.proceed();
        }

        Optional<HttpRequest<Object>> requestOpt = ServerRequestContext.currentRequest();
        if(requestOpt.isEmpty()) {
            LOG.warn("No request context available");
            throw new HttpStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }

        HttpRequest<Object> request = requestOpt.get();
        Optional<User> userOpt = request.getAttribute("currentUser", User.class);

        if(userOpt.isEmpty()) {
            LOG.warn("No user in request context");
            throw new HttpStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }

        User user = userOpt.get();
        UserRole userRole = user.getRole();

        boolean hasAccess = Arrays.asList(requiredRoles).contains(userRole);

        if(!hasAccess) {
            LOG.warn("User {} with role {} attempted to access endpoint requiring roles: {}",
                    user.getUsername(), userRole,Arrays.toString(requiredRoles));
            throw new HttpStatusException(HttpStatus.FORBIDDEN, "Insufficient permissions");
        }

        LOG.debug("authorization check passed for user: {} with role: {}", user.getUsername(), userRole);

        return context.proceed();
    }
}

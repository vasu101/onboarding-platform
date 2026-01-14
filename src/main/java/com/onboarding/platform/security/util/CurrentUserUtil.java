package com.onboarding.platform.security.util;

import com.onboarding.platform.security.model.User;
import io.micronaut.http.context.ServerRequestContext;

import java.util.Optional;

/**
 * Utility to access current authenticated user
 */
public class CurrentUserUtil {

    public static Optional<User> getCurrentUser() {
        return ServerRequestContext.currentRequest()
                .flatMap(request -> request.getAttribute("currentUser", User.class));
    }

    public static User getCurrentUserOrThrow() {
        return getCurrentUser()
                .orElseThrow(() -> new IllegalStateException("No authenticated user in context"));
    }

    public static String getCurrentUsername() {
        return getCurrentUser()
                .map(User::getUsername)
                .orElse("anonymous");
    }
}

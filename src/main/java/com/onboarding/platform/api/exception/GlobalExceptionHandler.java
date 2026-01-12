package com.onboarding.platform.api.exception;

import com.onboarding.platform.core.exception.*;
import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.server.exceptions.ExceptionHandler;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

/**
 * Global exception handler for API errors
 */
@Produces
@Singleton
@Requires(classes = {OnboardingException.class, ExceptionHandler.class})
public class GlobalExceptionHandler implements ExceptionHandler<OnboardingException, HttpResponse<GlobalExceptionHandler.ErrorResponse>> {

    private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @Override
    public HttpResponse<ErrorResponse> handle(HttpRequest request, OnboardingException exception) {
        LOG.error("Handling exception: {}", exception.getMessage(), exception);

        ErrorResponse errorResponse = new ErrorResponse(
                exception.getClass().getSimpleName(),
                exception.getMessage(),
                request.getPath(),
                Instant.now()
        );

        // Map specific exceptions to HTTP status codes
        return switch (exception) {
            case OnboardingNotFoundException onboardingNotFoundException -> HttpResponse.notFound(errorResponse);
            case OnboardingAlreadyExistsException onboardingAlreadyExistsException -> HttpResponse.badRequest(errorResponse);
            case InvalidStateTransitionException invalidStateTransitionException -> HttpResponse.badRequest(errorResponse);
            case MaxCorrectionAttemptsExceededException maxCorrectionAttemptsExceededException -> HttpResponse.badRequest(errorResponse);
            default -> HttpResponse.serverError(errorResponse);
        };
    }

    @Serdeable
    public static class ErrorResponse {
        private String error;
        private String message;
        private String path;
        private Instant timestamp;

        public ErrorResponse(String error, String message, String path, Instant timestamp) {
            this.error = error;
            this.message = message;
            this.path = path;
            this.timestamp = timestamp;
        }

        public String getError() {
            return error;
        }

        public String getMessage() {
            return message;
        }

        public String getPath() {
            return path;
        }

        public Instant getTimestamp() {
            return timestamp;
        }
    }
}

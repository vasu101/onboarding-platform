package com.onboarding.platform.security.service;

import com.onboarding.platform.security.model.UserRole;
import com.onboarding.platform.security.repository.UserRepository;
import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.runtime.server.event.ServerStartupEvent;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bootstrap service to create default users on application startup
 */
@Singleton
public class BootstrapService implements ApplicationEventListener<ServerStartupEvent> {

    private static final Logger LOG = LoggerFactory.getLogger(BootstrapService.class);

    private final AuthenticationService authenticationService;
    private final UserRepository userRepository;

    public BootstrapService(AuthenticationService authenticationService, UserRepository userRepository) {
        this.authenticationService = authenticationService;
        this.userRepository = userRepository;
    }

    @Override
    public void onApplicationEvent(ServerStartupEvent event) {
        LOG.info("Creating default users if they don't exist...");
        // Admin
        if(!userRepository.existsByUsername("admin")) {
            try{
                authenticationService.register(
                        "admin",
                        "admin@onboarding.com",
                        "admin@25",
                        "System Administrator",
                        UserRole.ADMIN
                );
                LOG.info("Default admin user created (username : admin, password: admin@25)");
            } catch (Exception e) {
                LOG.error("Failed to create admin user", e);
            }
        }

        // Reviewer
        if (!userRepository.existsByUsername("reviewer")) {
            try {
                authenticationService.register(
                        "reviewer",
                        "reviewer@onboarding.com",
                        "reviewer@25",
                        "System Reviewer",
                        UserRole.REVIEWER
                );
                LOG.info("Default reviewer user created (username: reviewer, password: reviewer@25)");
            } catch (Exception e) {
                LOG.error("Failed to create reviewer user", e);
            }
        }

        // Approver
        if (!userRepository.existsByUsername("approver")) {
            try {
                authenticationService.register(
                        "approver",
                        "approver@onboarding.com",
                        "approver@25",
                        "System Approver",
                        UserRole.APPROVER
                );
                LOG.info("Default approver user created (username: approver, password: approver@25)");
            } catch (Exception e) {
                LOG.error("Failed to create approver user", e);
            }
        }

        LOG.info("Bootstrap complete");
    }
}

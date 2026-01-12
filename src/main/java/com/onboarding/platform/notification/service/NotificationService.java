package com.onboarding.platform.notification.service;

import com.onboarding.platform.core.process.OnboardingProcess;
import com.onboarding.platform.core.state.OnboardingState;
import io.micronaut.scheduling.annotation.Async;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service for sending notifications (email, SMS, etc.)
 */
@Singleton
public class NotificationService {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationService.class);

    /**
     * Send notification when onboarding is submitted
     */
    @Async
    public void notifySubmitted(OnboardingProcess process) {
        LOG.info("Sending submission notification to {} for process {}",
                process.getSubject().getEmail(), process.getId());

        String email = process.getSubject().getEmail();
        String subject = "Onboarding Submitted Successfully";
        String body = String.format(
                "Hello %s,\n\n" +
                        "Your onboarding has been submitted and is now under review.\n" +
                        "Reference ID: %s\n\n" +
                        "You will be notified of any updates.\n\n" +
                        "Best regards,\n" +
                        "Onboarding Team",
                process.getSubject().getDisplayName(),
                process.getId()
        );

        sendEmail(email, subject, body);
    }

    /**
     * Send notification when corrections are requested
     */
    @Async
    public void notifyCorrectionRequested(OnboardingProcess process, String comments) {
        LOG.info("Sending correction request notification to {} for process {}",
                process.getSubject().getEmail(), process.getId());

        String email = process.getSubject().getEmail();
        String subject = "Action Required: Corrections Needed";
        String body = String.format(
                "Hello %s,\n\n" +
                        "Your onboarding requires corrections.\n" +
                        "Reference ID: %s\n\n" +
                        "Comments from reviewer:\n%s\n\n" +
                        "Please log in to make the necessary corrections.\n" +
                        "Attempt %d of %d\n\n" +
                        "Best regards,\n" +
                        "Onboarding Team",
                process.getSubject().getDisplayName(),
                process.getId(),
                comments,
                process.getCorrectionAttempts(),
                process.getMaxCorrectionAttempts()
        );

        sendEmail(email, subject, body);
    }

    /**
     * Send notification when verification fails
     */
    @Async
    public void notifyVerificationFailed(OnboardingProcess process, String details) {
        LOG.info("Sending verification failed notification to {} for process {}",
                process.getSubject().getEmail(), process.getId());

        String email = process.getSubject().getEmail();
        String subject = "Verification Failed";
        String body = String.format(
                "Hello %s,\n\n" +
                        "Unfortunately, the verification of your onboarding information has failed.\n" +
                        "Reference ID: %s\n\n" +
                        "Details: %s\n\n" +
                        "Please contact support for assistance.\n\n" +
                        "Best regards,\n" +
                        "Onboarding Team",
                process.getSubject().getDisplayName(),
                process.getId(),
                details
        );

        sendEmail(email, subject, body);
    }

    /**
     * Send notification when onboarding is approved
     */
    @Async
    public void notifyApproved(OnboardingProcess process) {
        LOG.info("Sending approval notification to {} for process {}",
                process.getSubject().getEmail(), process.getId());

        String email = process.getSubject().getEmail();
        String subject = "Onboarding Approved!";
        String body = String.format(
                "Hello %s,\n\n" +
                        "Congratulations! Your onboarding has been approved.\n" +
                        "Reference ID: %s\n\n" +
                        "Approved by: %s\n" +
                        "Approved at: %s\n\n" +
                        "Welcome aboard!\n\n" +
                        "Best regards,\n" +
                        "Onboarding Team",
                process.getSubject().getDisplayName(),
                process.getId(),
                process.getApprovedBy(),
                process.getApprovedAt()
        );

        sendEmail(email, subject, body);
    }

    /**
     * Send notification when onboarding is rejected
     */
    @Async
    public void notifyRejected(OnboardingProcess process, String reason) {
        LOG.info("Sending rejection notification to {} for process {}",
                process.getSubject().getEmail(), process.getId());

        String email = process.getSubject().getEmail();
        String subject = "Onboarding Rejected";
        String body = String.format(
                "Hello %s,\n\n" +
                        "Unfortunately, your onboarding has been rejected.\n" +
                        "Reference ID: %s\n\n" +
                        "Reason: %s\n\n" +
                        "If you believe this is an error, please contact support.\n\n" +
                        "Best regards,\n" +
                        "Onboarding Team",
                process.getSubject().getDisplayName(),
                process.getId(),
                reason
        );

        sendEmail(email, subject, body);
    }

    /**
     * Send notification when onboarding is completed
     */
    @Async
    public void notifyCompleted(OnboardingProcess process) {
        LOG.info("Sending completion notification to {} for process {}",
                process.getSubject().getEmail(), process.getId());

        String email = process.getSubject().getEmail();
        String subject = "Onboarding Completed";
        String body = String.format(
                "Hello %s,\n\n" +
                        "Your onboarding process is now complete!\n" +
                        "Reference ID: %s\n\n" +
                        "You can now access all features.\n\n" +
                        "Best regards,\n" +
                        "Onboarding Team",
                process.getSubject().getDisplayName(),
                process.getId()
        );

        sendEmail(email, subject, body);
    }

    /**
     * Generic notification based on state change
     */
    @Async
    public void notifyStateChange(OnboardingProcess process, OnboardingState oldState, OnboardingState newState) {
        LOG.info("State changed from {} to {} for process {}", oldState, newState, process.getId());

        // Route to specific notification based on new state
        switch (newState) {
            case SUBMITTED -> notifySubmitted(process);
            case PENDING_CORRECTION -> notifyCorrectionRequested(process, process.getCorrectionComments());
            case VERIFICATION_FAILED -> notifyVerificationFailed(process, process.getVerificationDetails());
            case APPROVED -> notifyApproved(process);
            case REJECTED -> notifyRejected(process, process.getRejectionReason());
            case COMPLETED -> notifyCompleted(process);
            default -> LOG.debug("No notification configured for state: {}", newState);
        }
    }

    // Mock email sending
    private void sendEmail(String to, String subject, String body) {
        LOG.info("=== MOCK EMAIL ===");
        LOG.info("To: {}", to);
        LOG.info("Subject: {}", subject);
        LOG.info("Body:\n{}", body);
        LOG.info("==================");

        // In production, integrate with:
        // - AWS SES
        // - SendGrid
        // - Mailgun
        // - etc.
    }

    // Mock SMS sending
    @Async
    public void sendSMS(String phoneNumber, String message) {
        LOG.info("=== MOCK SMS ===");
        LOG.info("To: {}", phoneNumber);
        LOG.info("Message: {}", message);
        LOG.info("================");

        // In production, integrate with:
        // - Twilio
        // - AWS SNS
        // - etc.
    }
}

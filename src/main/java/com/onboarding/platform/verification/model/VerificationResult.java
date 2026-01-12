package com.onboarding.platform.verification.model;

import io.micronaut.serde.annotation.Serdeable;

import java.util.ArrayList;
import java.util.List;

/**
 * Result of verification checks
 */
@Serdeable
public class VerificationResult {

    private boolean passed;
    private String summary;
    private List<VerificationCheck> checks;

    public VerificationResult() {
        this.checks = new ArrayList<>();
    }

    public VerificationResult(boolean passed, String summary) {
        this.passed = passed;
        this.summary = summary;
    }

    public boolean isPassed() {
        return passed;
    }

    public void setPassed(boolean passed) {
        this.passed = passed;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public List<VerificationCheck> getChecks() {
        return checks;
    }

    public void setChecks(List<VerificationCheck> checks) {
        this.checks = checks;
    }

    public void addCheck(VerificationCheck check) {
        this.checks.add(check);
    }

    /**
     * Individual verification check
     */
    @Serdeable
    public static class VerificationCheck {
        private String checkType;
        private boolean passed;
        private String details;

        public VerificationCheck(String checkType, boolean passed, String details) {
            this.checkType = checkType;
            this.passed = passed;
            this.details = details;
        }

        public String getCheckType() {
            return checkType;
        }

        public void setCheckType(String checkType) {
            this.checkType = checkType;
        }

        public boolean isPassed() {
            return passed;
        }

        public void setPassed(boolean passed) {
            this.passed = passed;
        }

        public String getDetails() {
            return details;
        }

        public void setDetails(String details) {
            this.details = details;
        }
    }
}

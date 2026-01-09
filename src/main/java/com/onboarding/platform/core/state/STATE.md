# State Definitions
```
State                     | Description
--------------------------|--------------------------------------------------------------
DRAFT                     | Onboarding has been created but not yet submitted.
SUBMITTED                 | Customer has submitted onboarding details for processing.
VERIFICATION_IN_PROGRESS  | Automated or manual verification is currently running.
VERIFICATION_FAILED       | Verification failed due to invalid or missing information.
PENDING_CORRECTION        | Customer is required to correct or resubmit information.
CORRECTED                 | Customer has submitted corrected information; verification restarts.
PENDING_APPROVAL          | Verification passed; awaiting final approval.
APPROVED                  | Onboarding has been approved by the authorized reviewer.
COMPLETED                 | Onboarding process is fully completed and finalized.
```

# State Transition Flow
```
DRAFT
└── submit()
↓
SUBMITTED
└── startVerification()
↓
VERIFICATION_IN_PROGRESS
↓
┌───────────────────────────────┐
│                               │
│ verificationFailed()          │ verificationPassed()
│                               │
↓                               ↓
VERIFICATION_FAILED         PENDING_APPROVAL
│                               ↓
│ requestCorrection()           approve()
↓                               ↓
PENDING_CORRECTION               APPROVED
│                               ↓
│ submitCorrections()           complete()
↓                               ↓
CORRECTED                     COMPLETED
│
└── (returns to VERIFICATION_IN_PROGRESS)
```
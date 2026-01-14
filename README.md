# Onboarding Platform

Enterprise-grade onboarding platform with workflow management, verification, and role-based access control.

## Features

- **Modular Monolith Architecture** - Clean separation of concerns with well-defined layers
- **Workflow Engine** - State machine-based onboarding process with validation
- **JWT Authentication** - Secure token-based authentication
- **Role-Based Access Control** - Four roles: CUSTOMER, REVIEWER, APPROVER, ADMIN
- **Audit Trail** - Complete history of all actions and state transitions
- **Verification Service** - Automated verification of onboarding data
- **Notification Service** - Email/SMS notifications for state changes
- **OpenAPI Documentation** - Interactive API documentation with Swagger UI

## Technology Stack

- **Framework**: Micronaut 4.x
- **Language**: Java 21
- **Database**: PostgreSQL
- **Authentication**: JWT
- **API Documentation**: OpenAPI 3.0 / Swagger
- **Build Tool**: Gradle
- **Database Migration**: Flyway

## Architecture

```
onboarding-platform/
├── core/              # Domain entities, enums, exceptions
├── workflow/          # Business logic and state management
├── verification/      # Verification service
├── notification/      # Notification service
├── audit/             # Audit logging
├── security/          # Authentication and authorization
└── api/               # REST controllers and DTOs
```

## Quick Start

### Prerequisites

- Java 21+
- PostgreSQL 15+
- Gradle 8+

### Setup

1. **Clone the repository**
```bash
git clone https://github.com/vasu101/onboarding-platform.git
cd onboarding-platform
```

2. **Start PostgreSQL**
```bash
    Use your credentials and create onboarding_db
```

3. **Run the application**
```bash
./gradlew run
```

The application will start on `http://localhost:8080`

### Access API Documentation

Once the application is running, access Swagger UI at:

**http://localhost:8080/swagger-ui/index.html**

## API Overview

### Authentication Endpoints

| Method | Endpoint | Description | Public |
|--------|----------|-------------|--------|
| POST | `/api/auth/register` | Register new user | Yes    |
| POST | `/api/auth/login` | Login and get JWT token | Yes    |

### Onboarding Endpoints

| Method | Endpoint | Description | Roles |
|--------|----------|-------------|-------|
| POST | `/api/onboarding` | Create new onboarding | CUSTOMER, ADMIN |
| GET | `/api/onboarding/{id}` | Get onboarding by ID | All authenticated |
| GET | `/api/onboarding` | List all onboardings | REVIEWER, APPROVER, ADMIN |
| POST | `/api/onboarding/{id}/submit` | Submit for review | CUSTOMER, ADMIN |
| POST | `/api/onboarding/{id}/request-correction` | Request corrections | REVIEWER, APPROVER, ADMIN |
| POST | `/api/onboarding/{id}/submit-corrections` | Submit corrections | CUSTOMER, ADMIN |
| POST | `/api/onboarding/{id}/start-verification` | Start verification | REVIEWER, ADMIN |
| POST | `/api/onboarding/{id}/auto-verify` | Auto-verify | REVIEWER, ADMIN |
| POST | `/api/onboarding/{id}/complete-verification` | Complete verification | REVIEWER, ADMIN |
| POST | `/api/onboarding/{id}/approve` | Approve onboarding | APPROVER, ADMIN |
| POST | `/api/onboarding/{id}/reject` | Reject onboarding | APPROVER, ADMIN |
| POST | `/api/onboarding/{id}/complete` | Complete onboarding | ADMIN |
| POST | `/api/onboarding/{id}/cancel` | Cancel onboarding | CUSTOMER, ADMIN |
| GET | `/api/onboarding/pending-review` | List pending review | REVIEWER, APPROVER, ADMIN |
| GET | `/api/onboarding/requiring-action` | List requiring action | CUSTOMER, ADMIN |

### Audit Endpoints

| Method | Endpoint | Description | Roles |
|--------|----------|-------------|-------|
| GET | `/api/audit/process/{id}` | Get full audit trail | All authenticated |
| GET | `/api/audit/process/{id}/transitions` | Get state transitions | All authenticated |

## User Roles

### CUSTOMER
- Create onboarding applications
- Submit applications for review
- Submit corrections when requested
- Cancel own applications
- View own applications

### REVIEWER
- View all applications
- Request corrections
- Start and complete verification
- View applications pending review

### APPROVER
- All REVIEWER permissions
- Approve or reject applications

### ADMIN
- Full system access
- All permissions from other roles
- Complete onboarding processes
- Manage system configuration

## Default Test Users

| Username | Password    | Role | Purpose |
|----------|-------------|------|---------|
| admin | admin@25    | ADMIN | Full system access |
| reviewer | reviewer@25 | REVIEWER | Review and verify |
| approver | approver@25 | APPROVER | Approve/reject |

## Onboarding Workflow

```
DRAFT → SUBMITTED → VERIFICATION_IN_PROGRESS → PENDING_APPROVAL → APPROVED → COMPLETED
           ↓                    ↓                      ↓
   PENDING_CORRECTION    VERIFICATION_FAILED      REJECTED
           ↓
       CORRECTED
```

### State Transitions

1. **DRAFT** → **SUBMITTED**: Customer submits application
2. **SUBMITTED** → **VERIFICATION_IN_PROGRESS**: Reviewer starts verification
3. **VERIFICATION_IN_PROGRESS** → **PENDING_APPROVAL**: Verification passes
4. **VERIFICATION_IN_PROGRESS** → **VERIFICATION_FAILED**: Verification fails
5. **PENDING_APPROVAL** → **APPROVED**: Approver approves
6. **PENDING_APPROVAL** → **REJECTED**: Approver rejects
7. **APPROVED** → **COMPLETED**: Admin completes onboarding

### Correction Flow

- Reviewers can request up to 3 corrections
- After max attempts, application is automatically rejected
- Customers can resubmit after making corrections

## Usage Examples

### 1. Register and Login

```bash
# Register
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john",
    "email": "john@example.com",
    "password": "password123",
    "fullName": "John Doe",
    "role": "CUSTOMER"
  }'

# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john",
    "password": "password123"
  }'

# Save the token from response
TOKEN="eyJhbGc..."
```

### 2. Create Onboarding

```bash
curl -X POST http://localhost:8080/api/onboarding \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "type": "INDIVIDUAL",
    "fullName": "John Doe",
    "email": "john@example.com",
    "phoneNumber": "+1234567890",
    "address": "123 Main St",
    "city": "New York",
    "country": "USA",
    "postalCode": "10001"
  }'
```

### 3. Submit for Review

```bash
curl -X POST http://localhost:8080/api/onboarding/{id}/submit \
  -H "Authorization: Bearer $TOKEN"
```

### 4. Auto-Verify (as Reviewer)

```bash
curl -X POST http://localhost:8080/api/onboarding/{id}/auto-verify \
  -H "Authorization: Bearer $REVIEWER_TOKEN"
```

### 5. Approve (as Approver)

```bash
curl -X POST http://localhost:8080/api/onboarding/{id}/approve \
  -H "Authorization: Bearer $APPROVER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "comments": "All checks passed"
  }'
```

### 6. View Audit Trail

```bash
curl -X GET http://localhost:8080/api/audit/process/{id} \
  -H "Authorization: Bearer $TOKEN"
```

## Configuration

Key configuration properties in `application.yml`:

```yaml
# JWT Settings
jwt:
  secret: "your-secret-key"
  expiration: 3600  # 1 hour

# Database
datasources:
  default:
    url: jdbc:postgresql://localhost:5432/<DB_NAME>
    username: <username>
    password: <password>

# OpenAPI
openapi:
  enabled: true

swagger-ui:
  enabled: true
```

## Security

- All endpoints except `/api/auth/*` require authentication
- JWT tokens expire after 1 hour (configurable)
- Passwords are hashed using SHA-256 with salt
- Role-based access control enforced at controller level
- Audit trail tracks all actions with user attribution

## Development

### Running Tests

```bash
./gradlew test
```

### Building

```bash
./gradlew build
```

### Creating Docker Image

```bash
./gradlew dockerBuild
```

## Production Considerations
### Currently, this is in development
- Need to implement real services for email/SMS
- Setup of SSL/TLS certificates
- And many more...

## Thank you, Let's contribute and make this a production ready setup
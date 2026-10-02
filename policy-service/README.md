# Policy Service — Policy Management System

Standalone, Maven-based Spring Boot 3 microservice handling **admin login
(single hardcoded credential pair, no entity/repository/service, no
self-registration)**, **policy registration (ADMIN only)**, **retrieval**,
and **search (US_03, ADMIN or CUSTOMER)**.

## Tech Stack
- Java 17
- Spring Boot 3.2.5
- Spring Security 6 (JWT — issues its own ADMIN tokens, validates
  CUSTOMER tokens issued by Customer Service)
- Spring Data JPA + MySQL (for policies only - no admin persistence)
- JJWT 0.11.5
- Lombok
- springdoc-openapi (Swagger UI, with Bearer auth support)
- Global Exception Handling

## Access Rules

| Endpoint | Method | Access |
|---|---|---|
| `/api/v1.0/admin/login` | POST | Public (predefined account - no registration) |
| `/api/v1.0/policy/register` | POST | **ADMIN only** (URL rule + `@PreAuthorize`) |
| `/api/v1.0/policy/getall` | GET | Any authenticated user (ADMIN or CUSTOMER) |
| `/api/v1.0/policy/searches` | GET | Any authenticated user (ADMIN or CUSTOMER) |
| `/api/v1.0/policy/{policyId}` | GET | Any authenticated user (ADMIN or CUSTOMER) |

Policy Service issues its own JWTs for admins (`/api/v1.0/admin/login`),
signed with the same `jwt.secret` that Customer Service uses to sign
CUSTOMER tokens. Either token type is accepted here for the GET/search
endpoints, since both are validated the same way (signature + expiry +
role claim) — only the `POST /register` path additionally checks for the
`ADMIN` role specifically.

## Database Setup
```
jdbc:mysql://localhost:3306/policy_service_db?createDatabaseIfNotExist=true
```
Hibernate `ddl-auto=update` creates `policies`, `policy_user_types`, and
now `admins` automatically.

## ⚠️ Before anything beyond local dev
`jwt.secret` in `application.properties` is a placeholder. It **must be
identical** to the value configured in Customer Service, or every
customer-issued token will fail signature verification here (and vice
versa). Replace with a securely generated, environment-injected secret
(≥256 bits) in real deployments.

## Build & Run
```bash
cd policy-service
mvn clean install
mvn spring-boot:run
```
Service starts on **http://localhost:8082**

## Swagger / OpenAPI
- Swagger UI: http://localhost:8082/swagger-ui.html
- OpenAPI JSON: http://localhost:8082/api-docs

## API Walkthrough

### 1. Log in as the admin

There is no admin entity, repository, service, or database row at all -
`AdminController` compares the submitted username/password directly
against two config values (`admin.default.username` /
`admin.default.password`, default **`admin` / `admin`**) and, if they
match, issues a JWT itself. Nothing is persisted or looked up in a table.

`POST /api/v1.0/admin/login`
```json
{ "username": "admin", "password": "admin" }
```
→ `200 OK` — `{ "token": "eyJhbGciOiJIUzI1NiJ9..." }`

> **Change the default password** (via `admin.default.password`) before
> deploying this anywhere beyond local development/demos.

### 2. Register a policy (ADMIN token required)
`POST /api/v1.0/policy/register`
```
Authorization: Bearer <admin-token>
```
```json
{
  "policyName": "Family Health Shield",
  "startDate": "2026-09-01",
  "durationYears": 10,
  "companyName": "Cognizant Insurance Ltd",
  "initialDeposit": 50000,
  "policyType": "HEALTH_INSURANCE",
  "userTypes": ["A", "B", "C"],
  "termsPerYear": 12,
  "termAmount": 2500,
  "interest": 6.5
}
```
Trying this with a CUSTOMER token (or no token) → `403 Forbidden` / `401 Unauthorized`.

### 3. Search policies (US_03) — ADMIN or CUSTOMER token
`GET /api/v1.0/policy/searches`

**Query parameters (all optional, but at least one is required):**

| Parameter | Type | Match behavior |
|---|---|---|
| `policyType` | enum (`VEHICLE_INSURANCE`, `TRAVEL_INSURANCE`, `LIFE_INSURANCE`, `HEALTH_INSURANCE`, `CHILD_PLANS`, `RETIREMENT_PLANS`) | exact |
| `years` | integer | exact match against `durationYears` |
| `companyName` | string | partial, case-insensitive |
| `policyId` | string | exact, case-insensitive |
| `policyName` | string | partial, case-insensitive |

Every supplied parameter narrows the result further (AND semantics).

```
GET /api/v1.0/policy/searches?policyType=HEALTH_INSURANCE&companyName=Cognizant
Authorization: Bearer <admin-or-customer-token>
```

Response (`200 OK`) — matches found:
```json
{
  "message": "1 policy found matching your search criteria.",
  "policies": [ { "policyId": "HI-2026-001", "...": "complete policy details" } ]
}
```

Response (`200 OK`) — no matches (US_03: "display user friendly message"):
```json
{
  "message": "No policies found matching your search criteria. Please try different search parameters.",
  "policies": []
}
```

Calling with **no parameters at all** → `400 Bad Request`:
```json
{ "message": "At least one search criterion (policyType, years, companyName, policyId, or policyName) must be provided" }
```

### 4. Get all policies / get by ID
`GET /api/v1.0/policy/getall`, `GET /api/v1.0/policy/{policyId}` — same
access rule as search (any authenticated role).

## Business Rules (unchanged from earlier phases)
- `startDate` must be on or after the current date.
- `policyId` auto-generated as `<SHORTCODE>-<YEAR>-XXX` (e.g. `HI-2026-001`).
- `endDate = startDate + durationYears`.
- `maturityAmount = initialDeposit + (durationYears * termsPerYear * termAmount) + (that * interest / 100)`.

## Testing

| File | Tests | Covers |
|---|---|---|
| `PolicyServiceImplTest` | 10 | Policy ID generation (first/incrementing sequence), maturity amount calc, end date calc, and US_03 search: type-only filter, exact policyId, AND-combination, friendly no-match message, partial company-name match, empty-criteria rejection |
| `AdminControllerTest` | 3 | Correct credentials issue a token, wrong password rejected, wrong username rejected |
| `PolicyServiceApplicationTests` | 1 | Spring context loads |

**14 tests total.** Not covered at this budget: controller-level
`@WebMvcTest`/security integration tests (e.g. asserting an actual `403`
HTTP response for a non-admin registering a policy). The service-layer
tests above verify the same business logic; the HTTP-layer wiring is
exercised manually per the walkthrough above.

## Project Structure
```
policy-service/
├── pom.xml
├── src/main/java/com/cognizant/policyservice/
│   ├── PolicyServiceApplication.java
│   ├── config/SwaggerConfig.java, SecurityConfig.java
│   ├── security/JwtUtil.java, JwtAuthenticationFilter.java
│   ├── entity/Policy.java, PolicyUserType.java, PolicyType.java, UserType.java
│   │        (no Admin entity - the admin credential is a config value, not a row)
│   ├── repository/PolicyRepository.java, PolicyUserTypeRepository.java
│   │        (no AdminRepository - nothing to query)
│   ├── dto/PolicyRequestDTO.java, PolicyResponseDTO.java, PolicySearchCriteria.java,
│   │        SearchResponseDTO.java, AdminLoginRequestDTO.java, (login handled by auth-service)
│   ├── service/PolicyService.java, service/impl/PolicyServiceImpl.java
│   │        (no AdminService - AdminController checks credentials inline)
│   ├── controller/PolicyController.java,
│   │              AdminController.java (compares against admin.default.username/password
│   │                                    directly and calls JwtUtil itself)
│   └── exception/ (PolicyNotFoundException, InvalidPolicyDataException,
│                    InvalidSearchCriteriaException, InvalidCredentialsException,
│                    ErrorResponse, GlobalExceptionHandler)
├── src/main/resources/application.properties
└── src/test/java/... (14 tests total, see Testing section above)
```

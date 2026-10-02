# Customer Service — Policy Management System

Standalone, Maven-based Spring Boot 3 microservice that handles **customer
registration, login, and lookup** for the Policy Management System.

## Tech Stack
- Java 17
- Spring Boot 3.2.5
- Spring Security 6 (issues AND validates its own JWTs — see Security below)
- Spring Data JPA + MySQL
- JJWT 0.11.5
- BCrypt password hashing
- Lombok
- springdoc-openapi (Swagger UI, with Bearer auth support)
- Bean Validation (Jakarta Validation)
- Global Exception Handling

## Security — Customer Service now issues its own tokens

**This changed from earlier phases.** Customer Service no longer only
*validates* tokens issued by a separate Auth Service — it now has its own
`password` column (BCrypt-hashed) and a `POST /login` endpoint that
authenticates a customer and issues a JWT directly. That same token is
what you pass to **Search Service** to search policies.

- `jwt.secret` in `application.properties` **must match** the value configured in
  Search Service, since both need to agree on the signing secret for HS256
  verification to succeed across services.
- `POST /api/v1.0/customer/register` and `POST /api/v1.0/customer/login`
  are both **public** — a new customer has no token yet, and login is how
  they get one.
- `GET /api/v1.0/customer/{id}` requires a valid bearer token from any
  authenticated role.
- The issued token's subject is the customer's **email**, with a `role`
  claim of `CUSTOMER`.

To log in and use the token:
```bash
curl -X POST http://localhost:8081/api/v1.0/customer/login \
  -H "Content-Type: application/json" \
  -d '{"email":"john.smith@example.com","password":"Str0ngPass!"}'
# -> { "token": "eyJhbGciOiJIUzI1NiJ9..." }

curl -H "Authorization: Bearer <token>" http://localhost:8081/api/v1.0/customer/1
```

**Demo mode:** set `app.security.enabled=false` to permit all requests
without any token at all (registration/login still work either way, since
they were already public).

## Prerequisites
- JDK 17+
- Maven 3.8+
- MySQL 8.x running locally (or update `application.properties`)
- Spring Tool Suite (STS) 4.x / any IDE with Lombok plugin installed

## Database Setup
The datasource is configured to auto-create the schema if it doesn't exist:
```
jdbc:mysql://localhost:3306/policy_customer_db?createDatabaseIfNotExist=true
```
Update `src/main/resources/application.properties` with your MySQL username/password.
Hibernate `ddl-auto: update` will create the `customers` table automatically
on first run, including the new `password` column.

> **If you already had customers registered before this change:** the
> `password` column is `NOT NULL`. Existing rows created before this
> update won't have a password and will fail to load / violate the
> constraint. For a dev database, easiest is to drop and let Hibernate
> recreate the `customers` table, or manually backfill a password hash for
> existing rows.

## Import into STS / Eclipse
1. Open STS → File → Import → Maven → Existing Maven Projects.
2. Browse to the `customer-service` folder and select the `pom.xml`.
3. Click Finish. STS will download dependencies and build the project.
4. Ensure the Lombok jar is installed into your IDE, then restart STS.

## Build & Run

### Using Maven
```bash
cd customer-service
mvn clean install
mvn spring-boot:run
```

### Using the packaged JAR
```bash
mvn clean package
java -jar target/customer-service.jar
```

The service starts on **http://localhost:8081**

## Swagger / OpenAPI
- Swagger UI: http://localhost:8081/swagger-ui.html
- OpenAPI JSON: http://localhost:8081/api-docs

## API Endpoints

### 1. Register Customer
`POST /api/v1.0/customer/register`

Sample Request Body:
```json
{
  "firstName": "John",
  "lastName": "Smith",
  "dob": "1990-05-15",
  "address": "12 MG Road, Chennai",
  "contactNo": "9876543210",
  "email": "john.smith@example.com",
  "password": "Str0ngPass!",
  "salary": 800000,
  "panNo": "ABCDE1234F",
  "employerType": "SALARIED",
  "employerName": "Acme Corp"
}
```

Business Rules enforced:
- `email` must be unique across the system (409 Conflict if duplicate).
- `password` (6-100 chars) is BCrypt-hashed before storage — never stored
  or returned in plain text, and never included in `CustomerResponseDTO`.
- If `employerType = SELF_EMPLOYED`, `employerName` must be blank/absent.
- If `employerType = SALARIED`, `employerName` is mandatory.
- `userType` is auto-calculated from salary (per year, in rupees):
  - ≤ 5 Lakhs → A
  - ≤ 10 Lakhs → B
  - ≤ 15 Lakhs → C
  - ≤ 30 Lakhs → D
  - > 30 Lakhs → E

### 2. Login
`POST /api/v1.0/customer/login`

```json
{
  "email": "john.smith@example.com",
  "password": "Str0ngPass!"
}
```
→ `200 OK`
```json
{ "token": "eyJhbGciOiJIUzI1NiJ9..." }
```
→ `401 Unauthorized` if the email doesn't exist or the password doesn't match.

### 3. Get Customer by ID
`GET /api/v1.0/customer/{customerId}`

## Error Response Format
```json
{
  "timestamp": "2026-08-25 10:15:30",
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid email or password",
  "path": "/api/v1.0/customer/login",
  "details": null
}
```

## Project Structure
```
customer-service/
├── pom.xml
├── src/main/java/com/cognizant/customerservice/
│   ├── CustomerServiceApplication.java
│   ├── config/
│   │   ├── SwaggerConfig.java
│   │   └── SecurityConfig.java          (PasswordEncoder bean, public register/login)
│   ├── security/
│   │   ├── JwtUtil.java                  (now GENERATES tokens too, not just validates)
│   │   └── JwtAuthenticationFilter.java  (populates SecurityContext from token claims)
│   ├── entity/Customer.java (+ password field), EmployerType.java, UserType.java
│   ├── repository/CustomerRepository.java
│   ├── dto/CustomerRequestDTO.java (+ password), CustomerResponseDTO.java,
│   │        LoginRequestDTO.java, (login handled by auth-service)
│   ├── service/CustomerService.java, service/impl/CustomerServiceImpl.java
│   ├── controller/CustomerController.java (+ /login)
│   └── exception/ (CustomerNotFoundException, EmailAlreadyExistsException,
│                    InvalidEmployerDetailsException, InvalidCredentialsException,
│                    ErrorResponse, GlobalExceptionHandler)
└── src/main/resources/application.properties   (jwt.secret + jwt.expiration-ms)
```

## How this connects to Search Service

1. Register a customer here (with a password).
2. Log in here to get a JWT.
3. Pass that JWT as `Authorization: Bearer <token>` when calling Search
   Service's `GET /api/v1.0/policy/searches`.

Search Service validates the token using the **same `jwt.secret`** — make
sure both services' `jwt.secret` values match exactly, or Search Service
will reject the token with a signature-verification failure.

Policy Service, by contrast, currently has **no authentication at all** —
policies can be created and read without any token. See Policy Service's
README for that change.

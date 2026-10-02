# API Gateway

API Gateway is the single public entry point for the Policy Management System.
It uses Eureka service discovery and Spring Cloud Gateway to route requests to
Auth, Customer, and Policy services.

## Port

`8080`

## Public routes

- `/api/v1.0/auth/**` -> `AUTH-SERVICE`
- `/api/v1.0/customer/**` -> `CUSTOMER-SERVICE`
- `/api/v1.0/policy/**` -> `POLICY-SERVICE`

The `/service/**` credential verification endpoints are intentionally not routed
through the gateway. Auth Service calls them directly using OpenFeign + Eureka.

## Startup order

1. service-registry :8761
2. customer-service :8081
3. policy-service :8085
4. auth-service :8082
5. api-gateway :8080

## Example

Instead of calling:

`http://localhost:8082/api/v1.0/auth/customer/login`

use:

`http://localhost:8080/api/v1.0/auth/customer/login`

Similarly, Customer and Policy APIs should be called through `:8080`.

## Swagger UI

The gateway exposes a combined Swagger UI for the three backend services:

- http://localhost:8080/swagger-ui/index.html
- http://localhost:8080/swagger-ui.html

The UI loads the OpenAPI documents from Auth Service, Customer Service, and Policy Service through Eureka-backed gateway routes.

Direct service Swagger UIs remain available on their respective service ports as before.

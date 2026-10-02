# Auth Service

Authentication service for the Policy Management System.

## Responsibilities
- Customer login
- Admin login
- JWT generation
- JWT expiration/signing configuration
- Service discovery registration with Eureka

## It does NOT own a user database
Customer credentials remain owned by `customer-service`; admin credentials remain owned by `policy-service`. Auth Service calls their internal authentication endpoints using OpenFeign, then issues the JWT.

## Endpoints
- `POST /api/v1.0/auth/customer/login`
  - body: `{ "username": "customer@example.com", "password": "..." }`
- `POST /api/v1.0/auth/admin/login`
  - body: `{ "username": "admin", "password": "admin" }`

## Port
`8082`

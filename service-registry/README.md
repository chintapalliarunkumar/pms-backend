# Service Registry

Eureka Server for the Policy Management System.

## Start

```bash
mvn spring-boot:run
```

The Eureka dashboard is available at:

`http://localhost:8761`

Start this application before `customer-service` and `policy-service`.

Registered services should appear as:
- CUSTOMER-SERVICE
- POLICY-SERVICE

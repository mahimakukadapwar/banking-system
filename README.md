#  Banking System API (Spring Boot + PostgreSQL + JWT)

A backend banking system built using Spring Boot with secure JWT authentication, role-based APIs, and transaction management.

##  Live Deployment
https://banking-app-mhgp.onrender.com/swagger-ui/index.html

##  Features
- User registration & login (JWT authentication)
- Account profile management
- Deposit / Withdraw / Transfer operations
- Transaction history tracking
- Daily transaction limits
- Account balance tracking
- Role-based security (Spring Security)
- RESTful APIs documented using Swagger UI

##  Tech Stack
- Java 17
- Spring Boot 3
- Spring Security + JWT
- PostgreSQL (Render cloud DB)
- Hibernate / JPA
- Swagger OpenAPI
- Maven

##  Authentication Flow
1. Register user
2. Login to get JWT token
3. Use token in Authorization header (Bearer <token>)
4. Access secured APIs

## API Documentation
Swagger UI:  
https://banking-app-mhgp.onrender.com/swagger-ui/index.html

## Example Modules
- Auth Controller → login/register
- User Controller → profile & account management
- Transaction Controller → banking operations

## Deployment
- Backend: Render
- Database: PostgreSQL (cloud)
- CI/CD: Git push auto-deploy

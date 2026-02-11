# ITSM API

A secure, role-based IT Service Management (ITSM) backend API built with Spring Boot and Keycloak. This application provides comprehensive authorization capabilities using custom user attributes and realm roles from Keycloak.

## Project Overview

**ITSM API** is a Spring Boot REST API that implements enterprise-grade security and authorization patterns. It demonstrates best practices for securing microservices with OAuth2, managing user permissions based on user type codes and status codes, and using Spring Security with Keycloak as an identity provider.

### Key Features

- **OAuth2 Resource Server**: Secured with Keycloak authentication
- **Role-Based Authorization**: Custom annotations for fine-grained access control
- **User Attributes Integration**: Extract and utilize Keycloak user attributes (`userTyCode`, `userSttusCode`, department, position)
- **AOP-Based Authorization**: Declarative authorization using annotations
- **RESTful API**: Demonstrates both protected and public endpoints
- **Spring Data JPA**: Database persistence layer

## Technology Stack

- **Java**: 17
- **Spring Boot**: 4.0.2
- **Spring Security**: OAuth2 Resource Server
- **Keycloak**: Identity and Access Management
- **Spring Data JPA**: ORM framework
- **MySQL**: Database
- **Lombok**: Reduce boilerplate code
- **AspectJ**: AOP implementation
- **Maven**: Build automation

## Project Structure

```
src/main/java/com/example/itsm_api/
├── ItsmApiApplication.java           # Application entry point
├── controller/
│   ├── ProtectedController.java       # Protected endpoint examples
│   └── PublicController.java          # Unrestricted endpoint examples
└── security/
    ├── CustomUserPrincipal.java       # Keycloak user attribute mapper
    ├── AuthorizationService.java      # Authorization business logic
    ├── AuthorizationAspect.java       # AOP authorization checker
    ├── RequireUserTyCode.java         # Custom annotation
    ├── RequireUserSttusCode.java      # Custom annotation
    └── SecurityConfig.java            # Spring Security configuration
```

## Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.6+
- Keycloak instance (for authentication)
- MySQL database

### Build the Project

```bash
# Using Maven wrapper
./mvnw clean package

# Or using system Maven
mvn clean package
```

### Run the Application

```bash
./mvnw spring-boot:run
```

The API will start on `http://localhost:8080` by default.

### Configuration

Update `src/main/resources/application.yml` to configure:

- **Keycloak Issuer URL**: OAuth2 token validation
- **Database Connection**: MySQL credentials and URL
- **Server Port**: Application port (default: 8080)

Example `application.yml`:

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: https://your-keycloak-instance/auth/realms/your-realm
  jpa:
    hibernate:
      ddl-auto: update
  datasource:
    url: jdbc:mysql://localhost:3306/itsm_db
    username: root
    password: your-password
```

## API Documentation

### Controllers

#### PublicController

Unrestricted endpoints accessible without authentication.

#### ProtectedController

Secured endpoints demonstrating various authorization patterns:

- **By User Type Code**: `@RequireUserTyCode("R005")`
- **By User Status Code**: `@RequireUserSttusCode("U002")`
- **By Role**: Using Spring Security `hasRole()` expressions
- **Custom Logic**: Using `AuthorizationService` for complex checks

### Security Architecture

See [AUTHORIZATION_GUIDE.md](AUTHORIZATION_GUIDE.md) for detailed information on:

- Authorization service implementation
- Custom annotation handling
- User attribute extraction
- Role mapping from Keycloak

## Building & Testing

### Compile

```bash
./mvnw clean compile
```

### Run Tests

```bash
./mvnw test
```

### Package

```bash
./mvnw clean package
```

## Documentation

- [Authorization Guide](AUTHORIZATION_GUIDE.md) - Detailed authorization implementation
- [Help](HELP.md) - Spring Boot resources and guides

## Common User Codes

### User Type Codes (userTyCode)

- `R001`: Manager
- `R005`: Regular Employee

### User Status Codes (userSttusCode)

- `U001`: Inactive
- `U002`: Active

## License

This project is licensed under the MIT License.

## Support

For issues, questions, or contributions, please refer to the related documentation files or contact your development team.

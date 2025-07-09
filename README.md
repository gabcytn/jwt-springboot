# Personal Spring Boot JWT Authentication Template

A simple and secure JWT authentication template built with Spring Boot.  
Designed for modern backend applications with token-based authentication, refresh token rotation, and Redis-based caching.

## Features

- Basic JWT authentication with access and refresh tokens
- Refresh tokens:
  - Hashed with SHA-256
  - Bound to device name for extra validation
  - Stored in Redis (7-day TTL)
  - Rotating — cannot be reused
  - Sent as HTTP-only cookies
- User details cached in Redis after authentication
- PostgreSQL for user registration and persistence
- Passwords hashed with BCrypt (12 rounds)

## Tech Stack

- **Spring Boot**
- **Redis** (for caching and token storage)
- **PostgreSQL** (user database)
- **JWT** (access/refresh token management)
- **BCrypt** (secure password hashing)

## Getting Started

1. **Clone the repository:**
   ```bash
   git clone https://github.com/gabcytn/jwt-springboot.git
   cd jwt-springboot
   ```
2. **Set up your application.properties**
   ```bash
   cp src/main/resources/example.application.properties src/main/resources/application.properties
   ```
3. **Run the application**
   ```bash
   ./mvnw spring-boot:run
   ```

# Gym Panel

Gym Panel is a modernized rebuild of my undergraduate final-year project. It keeps the original member, coach and administrator workflows and the original warm-yellow/dark-blue visual identity, while replacing the legacy implementation with a secure, maintainable stack.

![Java 21](https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk&logoColor=white)
![Spring Boot 4.1](https://img.shields.io/badge/Spring_Boot-4.1-6DB33F?logo=springboot&logoColor=white)
![Vue 3](https://img.shields.io/badge/Vue-3-42B883?logo=vuedotjs&logoColor=white)
![MySQL 8](https://img.shields.io/badge/MySQL-8-4479A1?logo=mysql&logoColor=white)

## Restored product scope

- Member portal: home, profile, coaches, coach appointments, courses, class bookings, equipment, equipment reservations, community posts, VR preview, membership e-card, coach chat and AI assistant.
- Coach portal: profile, member appointments, courses, community and member chat.
- Administrator portal: dashboard, users, coaches, notices, courses, bookings, equipment, equipment reservations and post moderation.
- Original URLs such as `/front/course`, `/front/equipment`, `/home`, `/user` and `/notice` remain available.

## Modernization

- Java 21, Spring Boot, Spring Security, MyBatis and Flyway.
- Vue 3, TypeScript and Vite with responsive layouts.
- Server sessions, BCrypt, CSRF protection and role-based authorization.
- Transactional class capacity checks and ownership checks on user actions.
- Optional Spring AI assistant; booking or cancellation changes require explicit confirmation.
- Environment-based secrets. The original `fyp_fitness` schema is untouched; this app uses `gym_portfolio`.

## Run locally in VS Code

Requirements: Java 21, Maven, Node.js 22+, MySQL 8.

Open the repository folder, then use two VS Code terminals.

PowerShell terminal 1:

```powershell
cd springboot
$env:DB_USERNAME = "root"
$env:DB_PASSWORD = "your-local-mysql-password"
mvn spring-boot:run
```

PowerShell terminal 2:

```powershell
cd vue
npm ci
npm run dev
```

Open `http://localhost:5173`. The API runs on `http://localhost:9090`.

Demo users share the password `GymDemo123!`:

| Role | Username |
| --- | --- |
| Member | `member` |
| Coach | `coach` |
| Administrator | `admin` |

## Verify

```powershell
cd springboot
mvn clean test

cd ../vue
npm run build
```

## Optional AI

The core app works with AI disabled. To enable an OpenAI-compatible provider:

```powershell
$env:AI_PROVIDER = "openai"
$env:AI_API_KEY = "your-key"
$env:AI_MODEL = "gpt-4o-mini"
```

Keep credentials in environment variables or an ignored local `.env` file.

## Docker

```bash
cp .env.example .env
docker compose up --build
```

Open `http://localhost:8080`.

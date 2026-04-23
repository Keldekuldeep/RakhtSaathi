# 🩸 RakhtSaathi Backend

Spring Boot REST API backend for RakhtSaathi - Emergency Blood Donation Platform.

## Tech Stack
- Java 17 + Spring Boot 3.2
- Spring Security + JWT Authentication
- Spring Data JPA + Hibernate
- MySQL 8

## Setup & Run

### Prerequisites
- Java 17+
- Maven 3.6+
- MySQL 8

### Steps
```bash
# 1. Create MySQL database
mysql -u root -p -e "CREATE DATABASE rakhtsaathi;"

# 2. Update application.properties
spring.datasource.password=YOUR_MYSQL_PASSWORD

# 3. Build
mvn clean package -DskipTests

# 4. Run
java -jar target/rakht-saathi-backend-1.0.0.jar
```

Server starts at: `http://localhost:8080`

## API Endpoints

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | /api/auth/register | Public | Register user |
| POST | /api/auth/login | Public | Login, get JWT |
| POST | /api/needy/profile | NEEDY | Create profile |
| GET | /api/needy/profile | NEEDY | Get my profile |
| POST | /api/requests | NEEDY | Create blood request |
| GET | /api/requests/{id} | Any | Get request status |
| GET | /api/requests/my | NEEDY | My requests |
| PUT | /api/requests/{id}/cancel | NEEDY | Cancel request |
| PUT | /api/requests/{id}/fulfill | NEEDY | Mark fulfilled |
| POST | /api/donor/profile | DONOR | Create profile |
| GET | /api/donor/notifications | DONOR | Blood requests |
| PUT | /api/donor/requests/{id}/accept | DONOR | Accept request |
| PUT | /api/donor/requests/{id}/reject | DONOR | Reject request |
| GET | /api/admin/dashboard | ADMIN | Stats |
| GET | /api/admin/requests | ADMIN | All requests |

## Default Credentials
- Admin: `admin@rakhtsaathi.com` / `admin123`

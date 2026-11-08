# RSM Student Answer Evaluation & Mastery Reporting API

## 1. Service Overview
This service is a backend REST API developed for the RSM Student Answer Evaluation & Mastery Reporting system. It enables automated student and problem management, dual-track answer grading (exact string comparison and numerical tolerance checks), and comprehensive concept mastery and parent progress reporting. Built with Java 17 and Spring Boot, it provides a robust, production-ready educational evaluation engine.

---

## 2. How to Run
### Prerequisites
- **Java 17** installed and configured in `JAVA_HOME`.
- Maven (use the included `./mvnw` wrapper).

### Running the Application
```bash
./mvnw spring-boot:run
```
- **Port**: `8080` (default)
- **H2 Console**: Accessible at `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:rsmdb`, Username: `sa`, Password: empty).
- **Startup Seed Data**: Automatically preloads 3 students (Alice, Bob, Charlie) and 5 problems covering linear equations and fractions upon startup.

---

## 3. API Endpoints Table

| Method | Path | Purpose | Example Request / Query Params |
|---|---|---|---|
| **POST** | `/students` | Register a new student | `{ "name": "Alice", "grade": 6 }` |
| **GET** | `/students` | Retrieve all students | None |
| **POST** | `/problems` | Create a new problem | `{ "concept": "linear-equations", "question": "2x = 6", "correctAnswer": "3", "tolerance": 0.01 }` |
| **GET** | `/problems` | Retrieve problems (optional concept filter) | `?concept=linear-equations` |
| **POST** | `/submissions` | Submit an answer and get immediate grading | `{ "studentId": 1, "problemId": 1, "answer": "3.0" }` |
| **GET** | `/students/{id}/mastery` | Get concept mastery breakdown | None |
| **GET** | `/students/{id}/report` | Get parent progress report | None |

### Example Request & Response for `POST /submissions`
**Request (`POST /submissions`):**
```json
{
  "studentId": 1,
  "problemId": 1,
  "answer": "3.0"
}
```

**Response (`201 Created`):**
```json
{
  "id": 1,
  "studentId": 1,
  "problemId": 1,
  "answer": "3.0",
  "correct": true,
  "score": 1.0,
  "correctAnswer": "3",
  "submittedAt": "2026-11-08T09:30:00Z"
}
```

---

## 4. Grading Rules & Mastery Formula

### Grading Rules (Dual-Track Algorithm)
1. **String Comparison**: Trims whitespace (`trim()`) and performs a case-insensitive comparison (`equalsIgnoreCase()`). If matched, marked correct (`isCorrect = true`, `score = 1.0`).
2. **Numeric Tolerance Comparison**: If both the student answer and the correct answer can be parsed as `Double` values, compute absolute difference $|s - c|$. If $\le \text{tolerance}$ (default `0.01` when problem tolerance is `null`), marked correct.
3. **Fallback**: Otherwise, marked incorrect (`isCorrect = false`, `score = 0.0`).

### Mastery Formula
For each concept:
$$\text{Mastery} = \frac{\text{Correct Submissions}}{\text{Total Attempts}}$$
Value ranges from `0.0` to `1.0`. Weakest concept is determined among concepts with $\ge 1$ attempts as the one with the lowest mastery score.

---

## 5. Design Trade-Offs
- **H2 In-Memory Database**: Chosen for fast execution, zero external setup overhead, and out-of-the-box support for unit and integration testing.
- **Unified Error Format**: All 4xx and 5xx responses strictly adhere to a standardized JSON structure (`timestamp`, `status`, `error`, `message`, `path`) to ensure predictable client-side error handling.
- **Stateless Grading Service**: Grading logic resides entirely in a dedicated domain service (`GradingService`), decoupling business rules from web controllers and persistence layers.

---

## 6. Database Migration (MS SQL / PostgreSQL)
To migrate from H2 to **PostgreSQL** or **MS SQL Server**, update `src/main/resources/application.properties` and include the respective JDBC driver in `pom.xml`:

### PostgreSQL Configuration
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/rsmdb
spring.datasource.driverClassName=org.postgresql.Driver
spring.datasource.username=db_user
spring.datasource.password=db_password
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.hibernate.ddl-auto=update
```

### MS SQL Server Configuration
```properties
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=rsmdb;encrypt=true;trustServerCertificate=true
spring.datasource.driverClassName=com.microsoft.sqlserver.jdbc.SQLServerDriver
spring.datasource.username=sa
spring.datasource.password=YourStrongPassword!
spring.jpa.database-platform=org.hibernate.dialect.SQLServerDialect
spring.jpa.hibernate.ddl-auto=update
```

---

## 7. Unfinished / Stretch Goals (Not Implemented)
1. **PDF Homework Generation & OCR Handwriting Scanning**: Automated paper test generation and image/handwriting recognition for student answers.
2. **Teacher Dashboard & Analytics UI**: Web frontend or administrative endpoints for teachers to monitor classroom-wide performance.
3. **Authentication & RBAC**: JWT-based security and role-based access control distinguishing students, parents, and teachers.
4. **Asynchronous Submission Queue**: Message queue (e.g., RabbitMQ/Kafka) integration for high-throughput batch grading and event-driven notifications.

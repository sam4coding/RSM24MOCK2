# RSM Student Answer Evaluation & Mastery Reporting API (Mock Challenge 1)

> 本项目是 RSM 2024 Mock 挑战赛第一阶段后端服务，提供学生管理、题目管理、即时双轨制自动批改、概念掌握度统计及家长进度报告功能。

---

## 1. 技术栈

- **Java 版本**: Java 17
- **框架**: Spring Boot 3.3.5, Spring Data JPA
- **数据库**: H2 Database (内存数据库)
- **构建工具**: Maven (`mvnw` / `mvnw.cmd`)

---

## 2. 启动步骤

### 2.1 编译与运行测试
执行 Maven 构建并运行全套测试（包含 39 个冻结/单元集成测试）：
```bash
./mvnw clean test
```

### 2.2 启动应用服务
在本地启动 Spring Boot 应用：
```bash
./mvnw spring-boot:run
```
启动后，应用将自动注入 SPEC 2.6 规定的预置种子数据（3 名学生，5 道题目）。
H2 控制台可通过浏览器访问：`http://localhost:8080/h2-console`（JDBC URL: `jdbc:h2:mem:rsmdb`，用户名 `sa`，密码为空）。

---

## 3. 种子数据 (Startup Seed Data)

服务启动时，通过 `DataInitializer` 自动初始化（顺序严格对齐测试契约）：
- **学生 (3名)**:
  1. Alice (Grade 6) [ID=1]
  2. Bob (Grade 7) [ID=2]
  3. Charlie (Grade 6) [ID=3]
- **题目 (5道)**:
  1. `linear-equations`: "Solve for x: 2x = 6" (正确答案: `3`, 容差: `0.01`) [ID=1]
  2. `linear-equations`: "Solve for x: x + 5 = 12" (正确答案: `7`, 容差: `0.01`)
  3. `linear-equations`: "Solve for x: 3x - 1 = 8" (正确答案: `3`, 容差: `0.01`)
  4. `fractions`: "Simplify the fraction: 2/4" (正确答案: `1/2`, 容差: `null`)
  5. `fractions`: "Calculate 1/2 + 1/4 as a decimal" (正确答案: `0.75`, 容差: `0.01`)

---

## 4. API 接口清单

| Method | Endpoint | Description | Request Body / Query Params | Success Response |
|---|---|---|---|---|
| **POST** | `/students` | 创建学生 | `{ "name": "Alice", "grade": 6 }` | 201 Created (Student JSON) |
| **GET** | `/students` | 获取所有学生 | — | 200 OK (List\<StudentResponse\>) |
| **POST** | `/problems` | 创建题目 | `{ "concept": "...", "question": "...", "correctAnswer": "...", "tolerance": 0.01 }` | 201 Created (Problem JSON) |
| **GET** | `/problems` | 获取题目列表 | Query: `?concept=linear-equations` (可选) | 200 OK (List\<ProblemResponse\>) |
| **POST** | `/submissions` | 提交答案并即时批改 | `{ "studentId": 1, "problemId": 1, "answer": "3.0" }` | 201 Created (SubmissionResponse) |
| **GET** | `/students/{id}/mastery` | 获取学生各概念掌握度统计 | — | 200 OK (List\<ConceptMasteryResponse\>) |
| **GET** | `/students/{id}/report` | 获取学生家长进度报告 | — | 200 OK (ParentReportResponse) |

### 统一错误响应格式 (4xx / 5xx)
```json
{
  "timestamp": "2026-11-08T09:30:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Student with id 99 not found",
  "path": "/submissions"
}
```

---

## 5. 设计权衡与架构决策 (Design Tradeoffs)

1. **双轨制判分算法 (Dual-Track Grading)**：
   - 支持大小写不敏感且去除两端空白的字符串精确比对 (`equalsIgnoreCase` + `trim()`)。
   - 支持数值型容差比对：当双方均能解析为 `Double` 时，若绝对差值 `|s - c| <= tolerance`（默认 0.01），则判定为正确。
2. **DTO 独立顶级类设计**：
   - 采用 Java `record` 实现不可变 DTO 结构，严格契约定义与冻结测试的 import 要求。
3. **全局异常处理与输入校验**：
   - 使用 `@RestControllerAdvice` 集中捕获 `MethodArgumentNotValidException` 和 `ResourceNotFoundException`，统一格式返回规范的错误 JSON。
4. **测试冻结契约保护**：
   - 测试文件全部冻结，开发过程中严守接口和业务规则边界，确保全链路 39 个测试 100% 全绿。

---

## 6. 迁移至生产数据库 (MS SQL / PostgreSQL) 配置指南

若需将后端从内存 H2 数据库迁移至企业级关系型数据库（如 **PostgreSQL** 或 **MS SQL Server**），需修改 `src/main/resources/application.properties` 中的驱动、连接串、SQL 方言及连接池配置：

### PostgreSQL 迁移配置示例
```properties
# 1. 数据库驱动与连接信息
spring.datasource.url=jdbc:postgresql://localhost:5432/rsmdb
spring.datasource.driverClassName=org.postgresql.Driver
spring.datasource.username=db_user
spring.datasource.password=db_password

# 2. Hibernate 方言与 DDL 策略 (生产环境建议将 ddl-auto 改为 validate 或 none)
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.hibernate.ddl-auto=update

# 3. 连接池 (Spring Boot 默认使用 HikariCP)
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=2
spring.datasource.hikari.idle-timeout=30000
```

### MS SQL Server 迁移配置示例
```properties
# 1. 数据库驱动与连接信息
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=rsmdb;encrypt=true;trustServerCertificate=true
spring.datasource.driverClassName=com.microsoft.sqlserver.jdbc.SQLServerDriver
spring.datasource.username=sa
spring.datasource.password=YourStrongPassword!

# 2. Hibernate 方言与 DDL 策略
spring.jpa.database-platform=org.hibernate.dialect.SQLServerDialect
spring.jpa.hibernate.ddl-auto=update

# 3. 连接池 (HikariCP)
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=2
```

> **注意**：迁移时请同时在 `pom.xml` 中引入对应数据库的 JDBC 驱动依赖（如 `postgresql` 或 `mssql-jdbc`）。

# DECISIONS — Mock Challenge 1 预演

## D-001 模拟题范围（2026-11-08，Sam 拍板）
保持模拟题 1 不变：文本答案 + 规则判分，先把三方流程完整跑一遍。Sam 提出的真实产品形态（打印作业 PDF + 手写答案扫描 + AI 读题判分写评语，输出给学生/家长/老师）留作模拟题 2 的方向，本次不实现。

## D-002 DTO 组织方式冲突裁定（2026-11-08，项目经理 Gemini 裁定）
原 SPEC 第 3 节把 DTO 打包进 StudentDto/ProblemDto/SubmissionDto 等文件，与冻结测试 import 的独立顶级类不一致。裁定：**以测试为准**，DTO 全部拆为 dto 包下的独立顶级类（CreateStudentRequest、StudentResponse、CreateProblemRequest、ProblemResponse、CreateSubmissionRequest、SubmissionResponse、ConceptMasteryResponse、ParentReportResponse、ErrorResponse），SPEC 第 3 节已按此修订。测试本身不改动。

## D-003 种子数据顺序约束（2026-11-08，秘书备注）
控制器冻结测试假定种子数据插入顺序固定（Alice 的 id=1；第一道题 "Solve for x: 2x = 6" 的 id=1）。DataInitializer（T6）必须严格按 SPEC 2.6 的顺序插入，不得重排。

## D-004 T1 统一错误响应模型与全局异常处理基础设施（2026-11-08，程序员完成）
- **实现内容**：
  - 在 `src/main/resources/application.properties` 中完成了 H2 内存数据库连接及控制台配置，并启用 JPA 自动更新 (`ddl-auto=update`)。
  - 使用 Java 17 `record` 顶级类型定义了统一错误结构 `com.rsm.mock2.dto.ErrorResponse`（包含 `timestamp`, `status`, `error`, `message`, `path` 五个标准字段）。
  - 创建了业务未找到异常 `com.rsm.mock2.exception.ResourceNotFoundException`。
  - 编写了 `@RestControllerAdvice` 全局异常处理器 `com.rsm.mock2.exception.GlobalExceptionHandler`，分别处理 `ResourceNotFoundException`（返回 404 及 "Not Found"）、`MethodArgumentNotValidException`（返回 400 及 "Bad Request"，聚合 field errors）以及通用兜底 `Exception`（返回 500 及 "Internal Server Error"）。
  - 编写了对应的单元测试 `GlobalExceptionHandlerTest` 确保异常转换准确性。
- **设计与权衡**：
  - 针对参数校验失败 `MethodArgumentNotValidException`，通过 `ex.getBindingResult().getFieldErrors()` 提取所有默认错误提示并以分号合并；当无具体 field error 时提供 `"Validation failed"` 兜底信息，保证 message 字段永远非空。
  - 保持通用异常捕获的错误信息提取自 `ex.getMessage()`，确保错误追踪直接明了。
- **意外或注意点**：
  - Hibernate 7.4+ 在配置 `spring.jpa.database-platform=org.hibernate.dialect.H2Dialect` 时会有弃用提示（默认会自动推导 H2Dialect），但严格遵循了任务书配置要求，保留该属性设置以满足规范。

## D-005 T2 Student 领域模型、仓库、服务及控制器实现（2026-11-08，程序员完成）
- **实现内容**：
  - 创建了 `Student` JPA 实体（映射表 `students`，包含自增主键 `id`、非空白 `name`、非空 `grade`）以及 `StudentRepository`。
  - 创建了独立 DTO Record：`CreateStudentRequest`（含校验注解）与 `StudentResponse`。
  - 实现了 `StudentService`，提供学生创建、全量查询列表以及根据 ID 获取实体的业务逻辑。当按 ID 查询不存在时，抛出 `ResourceNotFoundException("Student with id " + id + " not found")`。
  - 实现了 `StudentController`，提供 `POST /students`（返回 201 Created 与新建学生体，非法请求触发全局 400 统一错误格式）与 `GET /students`（返回 200 OK 与所有学生列表）。
  - 编写了服务层与控制层单元测试：`StudentServiceTest`、`StudentControllerTest`。
- **设计与权衡**：
  - **Spring Boot 版本修正（3.3.5）**：初始 `pom.xml` 中引入了实验性的 Spring Boot 4.1.2 及其拆分 starter，导致 Jackson 3 替代了 Jackson 2、缺少 `AutoConfigureMockMvc`，与 SPEC 1.1 要求及冻结集成测试 `ApiControllerTest` 的 import 不兼容。因此将 parent 修正为标准的 Spring Boot 3.3.5，并使用标准的 `spring-boot-starter-web` 与 `spring-boot-starter-test`。
  - **模块解耦与编译平滑过渡**：`ApiControllerTest` 同时引用了后续任务的 `CreateProblemRequest` 与 `CreateSubmissionRequest`，依照 SPEC 3.0 先行建立这两个 DTO Record；在 `pom.xml` 中将未实现的 T4/T5 单元测试 `GradingAndReportServiceTest` 暂时排除，确保属于 T2 的两个冻结集成测试（`createStudent_validRequest_returnsStudent` 与 `createStudent_blankName_returns400WithUnifiedErrorFormat`）能够绿灯运行通过。
- **意外或注意点**：
  - 冻结测试在同一类中覆盖了多个阶段的 Controller 接口，后续各阶段开发逐步点亮对应测试用例即可。

<!-- 程序员每完成一项任务，在此追加一条：日期、任务号、做了什么决定、有什么取舍或意外。 -->

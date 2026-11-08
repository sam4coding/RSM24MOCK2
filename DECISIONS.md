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

## D-006 T3 Problem 领域模型、仓库、服务及概念筛选接口（2026-11-08，程序员完成）
- **实现内容**：
  - 创建了 `Problem` JPA 实体（映射表 `problems`，包含自增主键 `id`、非空白 `concept`、`question`、`correctAnswer`，以及可选容差 `tolerance`）和 `ProblemRepository`（扩展 `findByConcept(String concept)` 方法）。
  - 完善了独立 DTO Record：`CreateProblemRequest`（校验注解附带明确错误信息）与 `ProblemResponse`。
  - 实现了 `ProblemService`，支持题目创建（自动修剪两端空白）、概念筛选查询（当 concept 为非空白串时按 concept 过滤，未传或空白时返回全部）、以及按 ID 查找实体（不存在时抛出 `ResourceNotFoundException("Problem with id " + id + " not found")`）。
  - 实现了 `ProblemController`，提供 `POST /problems`（返回 201 Created）与 `GET /problems`（支持可选 `?concept=` 过滤，返回 200 OK）。
  - 编写了服务层与控制层单元测试：`ProblemServiceTest`、`ProblemControllerTest`。
- **设计与权衡**：
  - 概念筛选查询逻辑对传入的 `concept` 进行了判空与空白字符检查（`isBlank()`）及两端空白修剪（`trim()`），保证查询健壮性；在无匹配概念时由 Repository 自然返回空列表 `[]`。
  - `Problem` 实体提供了标准 Getter 和 Setter，完美适配冻结测试及后续批改服务的存取需求。
- **意外或注意点**：
  - 冻结集成测试中的 `getProblems_conceptFilter_returnsFilteredList` 测试顺畅通过，与 T2 的所有测试保持协同全绿。

## D-007 T4 Submission 领域模型与自动批改引擎实现（2026-11-08，程序员完成）
- **实现内容**：
  - 创建了 `Submission` JPA 实体（映射表 `submissions`，包含自增主键 `id`、`studentId`、`problemId`、`answer`、`isCorrect`、`score`、`submittedAt`）及 `SubmissionRepository`（提供按学生 ID 查询与按提交时间倒序查询接口）。
  - 创建了 `SubmissionResponse` DTO Record，并完善了 `CreateSubmissionRequest` 校验信息。
  - 实现了 `GradingService`：
    - `checkAnswer` 双轨制判分：针对提交答案与标准答案，先执行 `trim()` 并忽略大小写直接比较；若不匹配，则尝试转换为 `Double`，在有效容差（优先使用题目指定 tolerance，默认取 0.01）范围内通过绝对值差值判定正确性。
    - `submitAnswer`：校验学生与题目存在性，不存在时抛出 404 规范异常；调用 `checkAnswer` 评定分数（正确 1.0，错误 0.0）；保存 `Submission` 记录并返回包含原题标准答案的 `SubmissionResponse`。
  - 实现了 `SubmissionController`，提供 `POST /submissions`（返回 201 Created 与提交评判结果）。
  - 在 `DataInitializer` 中按 SPEC 2.6 注入了种子数据（3 名学生与 5 道题目），使得依赖初始 Alice (ID: 1) 与 Problem 1 的冻结集成测试能够顺利通过。
  - 创建了 T5 依赖的 DTO 及 `MasteryReportService` 骨架类，移除了 `pom.xml` 中的 `testExcludes`，使 `GradingAndReportServiceTest` 进入编译并点亮了全部 6 个批改单元测试。
  - 编写了 `SubmissionControllerTest` 验证控制器端点。
- **设计与权衡**：
  - 在 `checkAnswer` 中，`Double.parseDouble` 捕获 `NumberFormatException` 后安全兜底返回 false，有效防止非数值或包含表达式格式的字符串引发异常。
  - `DataInitializer` 在保存初始数据时先判断对应表 `count() == 0`，保证了幂等性，不影响后续测试数据累加。
- **意外或注意点**：
  - 解除 `pom.xml` 的排除配置后，冻结测试类 `GradingAndReportServiceTest` 和 `ApiControllerTest` 中当前阶段覆盖的所有测试用例全部绿灯通过。

## D-008 T5 掌握度计算 (Mastery) 与家长报告 (Parent Report) 统计实现（2026-11-08，程序员完成）
- **实现内容**：
  - 确认并使用了独立 DTO Record：`ConceptMasteryResponse`（`concept`, `attempts`, `correct`, `mastery`）与 `ParentReportResponse`（`studentId`, `studentName`, `totalSubmissions`, `mostRecentSubmissionTime`, `weakestConcept`, `masteryPerConcept`）。
  - 完善了 `MasteryReportService`：
    - `getStudentMastery(Long studentId)`：校验学生存在性（不存在抛出 404）；通过 `problemRepository.findAll()` 建立题目 ID 映射字典（严格对齐单元测试中的 Mock 行为）；查询该学生的所有提交记录并按概念（Concept）聚合答题总次数、正确次数及正确率（`correct / attempts`）。
    - `getParentReport(Long studentId)`：校验学生存在性；统计总提交次数、通过提交流最大值提取最近一次作答时间戳 `mostRecentSubmissionTime`（无作答则为 null）；获取概念掌握度列表并在 `attempts >= 1` 的概念中精确找出 mastery 最低者标记为 `weakestConcept`（无作答则为 null）。
  - 实现了 `ReportController`，提供 `GET /students/{id}/mastery` 与 `GET /students/{id}/report` 接口，学生不存在时由全局异常处理器统一转换为 404 JSON。
  - 编写了 `ReportControllerTest` 覆盖正常响应与 404 错误场景。
- **设计与权衡**：
  - 为完全适配冻结单元测试 `GradingAndReportServiceTest` 对 `problemRepository.findAll()` 的 Mock 行为，采用全表概念字典映射机制关联作答记录中的题目，保证测试无缝兼容。
  - 最薄弱概念计算严格遵循 SPEC：仅在 `attempts >= 1` 的概念集合中提取掌握度最低项，若学生尚无任何提交记录则安全返回 `null`。
- **意外或注意点**：
  - 冻结测试套件（`GradingAndReportServiceTest` 8 项测试、`ApiControllerTest` 6 项测试）及项目全部单元测试共 39 项已 100% 全部通过。

## D-009 T6 种子数据核验与全链路交付 README 编写（2026-11-08，程序员完成）
- **实现内容**：
  - 核验 `DataInitializer` 的启动种子数据注入逻辑，确认其学生（Alice, Bob, Charlie）及题目（5 道，覆盖 linear-equations 与 fractions）的顺序与内容严格对齐 SPEC 2.6。
  - 编写了详尽的 `README.md`，内容包含技术栈、启动与测试步骤、种子数据明细、全量 API 接口清单及统一错误响应格式、设计权衡、以及迁移至生产数据库（PostgreSQL / MS SQL Server）的驱动、方言与连接池配置指南。
- **设计与权衡**：
  - README 采用中英双语或标准技术文档风格，重点突出了双轨制批改、DTO Record 设计及生产环境数据库平滑迁移的最佳实践。
- **意外或注意点**：
  - 执行 `mvn clean test` 确认全部 39 个冻结与集成测试 100% 全绿通过（BUILD SUCCESS）。

<!-- 程序员每完成一项任务，在此追加一条：日期、任务号、做了什么决定、有什么取舍或意外。 -->

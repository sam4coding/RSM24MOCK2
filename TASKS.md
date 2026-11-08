# TASKS — Mock Challenge 1（项目经理：Gemini AI Studio）

> 规则：一次只做一项；每项完成的标准是验收通过 + commit。测试文件已冻结，任何人不得修改。

- [x] **T1 统一错误响应模型与全局异常拦截器**
  - 文件：dto/ErrorResponse、exception/ResourceNotFoundException、exception/GlobalExceptionHandler、src/main/resources/application.properties（H2 内存数据库与 JPA 配置）
  - 验收：触发 ResourceNotFoundException 返回 404，响应体含 timestamp, status, error, message, path 且 error="Not Found"；@Valid 失败返回 400 且 error="Bad Request"；`mvn clean compile` 通过

- [ ] **T2 Student 模块**（Entity、Repository、DTO、Service、Controller 与校验）
  - 文件：domain/Student、repository/StudentRepository、dto/CreateStudentRequest、dto/StudentResponse、service/StudentService、controller/StudentController
  - 验收：POST /students 合法数据返回 201（或 200）及 student JSON；name 空白或 grade 为空返回 400 统一错误格式；GET /students 返回全部学生

- [ ] **T3 Problem 模块**（Entity、Repository、DTO、Service、Controller 与概念筛选）
  - 文件：domain/Problem、repository/ProblemRepository、dto/CreateProblemRequest、dto/ProblemResponse、service/ProblemService、controller/ProblemController
  - 验收：POST /problems 成功创建；GET /problems 不带参数返回全部，带 ?concept=xxx 仅返回对应概念，无匹配返回空数组 []

- [ ] **T4 Submission 模块与自动批改引擎**
  - 文件：domain/Submission、repository/SubmissionRepository、dto/CreateSubmissionRequest、dto/SubmissionResponse、service/GradingService、controller/SubmissionController
  - 验收：字符串比较两端 trim 且大小写不敏感；双方可转浮点时按容差（默认 0.01）判定；响应含 correct、score（1.0/0.0）、correctAnswer；studentId 或 problemId 不存在返回 404 统一错误格式

- [ ] **T5 掌握度计算与家长报告**
  - 文件：dto/ConceptMasteryResponse、dto/ParentReportResponse、service/MasteryReportService、controller/ReportController
  - 验收：GET /students/{id}/mastery 按 concept 汇总 attempts、correct、mastery；GET /students/{id}/report 含 studentName、总提交次数、最近提交时间、概念列表、weakestConcept（attempts ≥ 1 中最低者）；学生不存在返回 404

- [ ] **T6 种子数据与全链路交付 README**
  - 文件：config/DataInitializer、README.md
  - 验收：启动后内置 3 名学生与 5 道题目（覆盖 2 个概念，插入顺序严格按 SPEC 2.6）；`mvn clean test` 全部冻结测试全绿；README 含启动步骤、接口清单、设计权衡、迁移 MS SQL / PostgreSQL 需更改的配置（驱动、dialect、连接池）

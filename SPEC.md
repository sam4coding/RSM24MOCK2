# SPEC — RSM Student Answer Evaluation & Mastery Reporting API (Mock Challenge 1)

> 项目经理：Gemini AI Studio。第 3 节为 2026-11-08 裁定后的修订版（DTO 全部拆为独立顶级类，与冻结测试的 import 一致）。

## 1. 系统数据模型

### 1.1 实体设计（JPA Entities）

**(1) Student**（表名：students）
- id: Long, 主键，自增（@GeneratedValue(strategy = GenerationType.IDENTITY)）
- name: String, 非空且不可为空白（@NotBlank）
- grade: Integer, 非空（@NotNull），例如 6

**(2) Problem**（表名：problems）
- id: Long, 主键，自增
- concept: String, 非空（@NotBlank），如 "linear-equations", "fractions"
- question: String, 非空（@NotBlank），题目描述文本
- correctAnswer: String, 非空（@NotBlank），标准答案字符串
- tolerance: Double, 可为空（null 表示未显式指定，批改时默认取 0.01）

**(3) Submission**（表名：submissions）
- id: Long, 主键，自增
- studentId: Long, 非空，外键逻辑关联学生 ID
- problemId: Long, 非空，外键逻辑关联题目 ID
- answer: String, 非空，学生提交的原始答案
- isCorrect: boolean, 是否正确
- score: Double, 得分（正确为 1.0，错误为 0.0）
- submittedAt: Instant, 提交时间戳（默认系统当前时间）

### 1.2 统一错误响应格式

任何 4xx / 5xx 异常响应（含参数校验失败 400 与资源不存在 404）必须严格输出如下 JSON 结构：

```json
{
  "timestamp": "2026-11-08T09:30:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Student with id 99 not found",
  "path": "/submissions"
}
```

## 2. API 接口契约与业务规则

### 2.1 学生管理

**POST /students**
- 请求体：`{ "name": "Alice", "grade": 6 }`
- 校验规则：name 不能为空或空白串（@NotBlank），grade 不能为空（@NotNull）。不满足时返回 400 + 统一错误格式。
- 成功响应：201 Created（或 200 OK），返回新建的 Student 对象及分配的 id。

**GET /students**
- 成功响应：200 OK，返回所有学生的数组列表 List\<StudentResponse\>。

### 2.2 题目管理

**POST /problems**
- 请求体：`{ "concept": "linear-equations", "question": "Solve for x: 2x = 6", "correctAnswer": "3", "tolerance": 0.01 }`
- 校验规则：concept, question, correctAnswer 均为 @NotBlank；tolerance 选填。
- 成功响应：201 Created，返回新建的 Problem 实体 JSON。

**GET /problems**
- Query 参数：concept（可选字符串）。
- 若未传 concept，返回全部题目列表；若传 concept（如 ?concept=linear-equations），仅返回属于该概念的题目列表。
- 成功响应：200 OK，返回 List\<ProblemResponse\>。

### 2.3 提交与即时评分

**POST /submissions**
- 请求体：`{ "studentId": 1, "problemId": 2, "answer": "3.0" }`
- 前置检查：studentId 对应的学生必须存在，否则抛出 ResourceNotFoundException("Student with id X not found")，返回 404；problemId 对应的题目必须存在，否则抛出 ResourceNotFoundException("Problem with id X not found")，返回 404。answer 不可为 null。
- 批改判定双轨制算法（Grading Rules）：
  1. 提取 answer.trim() 与 correctAnswer.trim()。
  2. 大小写不敏感字符串比对：若 submittedTrim.equalsIgnoreCase(correctTrim) 为 true，则直接判定为正确。
  3. 浮点数容差比对：若双方均能被 Double.parseDouble(...) 成功解析为数值，设学生值为 s、标准值为 c、容差 t（题目 tolerance 为 null 时取默认 0.01）。若 |s − c| ≤ t，则判定为正确。
  4. 若均不满足，则判定为错误。
- 计分：正确时 isCorrect = true, score = 1.0；错误时 isCorrect = false, score = 0.0。
- 重复提交规则：同一学生可对同一题目多次提交，每次提交均持久化为独立的 Submission 记录，并在掌握度统计中各自计为一次 attempt。
- 响应体（必须包含 correct, score, 以及题目原始的 correctAnswer）：

```json
{
  "id": 1,
  "studentId": 1,
  "problemId": 2,
  "answer": "3.0",
  "correct": true,
  "score": 1.0,
  "correctAnswer": "3",
  "submittedAt": "2026-11-08T09:30:00Z"
}
```

### 2.4 概念掌握度

**GET /students/{id}/mastery**
- 前置检查：studentId 必须存在，否则返回 404。
- 计算规则：查询该学生所有的 submissions，并联合题目关联获取其 concept。按 concept 分组汇总：
  - attempts：提交总次数（count）
  - correct：正确提交次数（count(isCorrect == true)）
  - mastery：正确率 = correct / attempts（值域 [0.0, 1.0]）
- 响应体：

```json
[
  { "concept": "linear-equations", "attempts": 4, "correct": 3, "mastery": 0.75 },
  { "concept": "fractions", "attempts": 2, "correct": 1, "mastery": 0.5 }
]
```

### 2.5 家长进度报告

**GET /students/{id}/report**
- 前置检查：studentId 必须存在，否则返回 404。
- 计算字段：
  - studentId: Long
  - studentName: String
  - masteryPerConcept: List\<ConceptMasteryResponse\>（同 2.4）
  - totalSubmissions：该学生总提交次数
  - mostRecentSubmissionTime：最近一次提交时间戳 Instant（若从未提交则为 null）
  - weakestConcept：最薄弱概念（在 attempts >= 1 的概念中 mastery 最低者；平局取其一；若该学生尚未提交过任何作答，则为 null）
- 响应体：

```json
{
  "studentId": 1,
  "studentName": "Alice",
  "totalSubmissions": 6,
  "mostRecentSubmissionTime": "2026-11-08T09:35:00Z",
  "weakestConcept": "fractions",
  "masteryPerConcept": [
    { "concept": "linear-equations", "attempts": 4, "correct": 3, "mastery": 0.75 },
    { "concept": "fractions", "attempts": 2, "correct": 1, "mastery": 0.5 }
  ]
}
```

### 2.6 种子数据（Seed Data on Startup）

服务启动后由 CommandLineRunner 自动预置（**插入顺序不可变 — 控制器冻结测试假定 Alice 的 id=1、第一道题的 id=1**）：

3 名学生（按此顺序）：
1. Alice, Grade 6
2. Bob, Grade 7
3. Charlie, Grade 6

5 道题目（覆盖 2 个概念，按此顺序）：
1. Concept: linear-equations, Question: Solve for x: 2x = 6, Correct: 3, Tolerance: 0.01
2. Concept: linear-equations, Question: Solve for x: x + 5 = 12, Correct: 7, Tolerance: 0.01
3. Concept: linear-equations, Question: Solve for x: 3x - 1 = 8, Correct: 3, Tolerance: 0.01
4. Concept: fractions, Question: Simplify the fraction: 2/4, Correct: 1/2, Tolerance: null
5. Concept: fractions, Question: Calculate 1/2 + 1/4 as a decimal, Correct: 0.75, Tolerance: 0.01

## 3. 文件清单与类设计规范（修订版，2026-11-08 裁定）

项目根包路径统一为：com.rsm.mock2

| 文件完整相对路径 | 类型 | 职责、构造器与关键方法/字段签名 |
|---|---|---|
| dto/ErrorResponse.java | record | 统一错误响应结构：record ErrorResponse(Instant timestamp, int status, String error, String message, String path) |
| exception/ResourceNotFoundException.java | class | 404 异常：继承 RuntimeException，构造器 ResourceNotFoundException(String message) |
| exception/GlobalExceptionHandler.java | class | @RestControllerAdvice 全局异常处理器：handleNotFound → 404；handleValidation(MethodArgumentNotValidException) → 400；handleGeneral(Exception) → 500 |
| domain/Student.java | class | JPA 实体：@Entity @Table(name = "students")；Long id (PK, IDENTITY), String name (@NotBlank), Integer grade (@NotNull) |
| repository/StudentRepository.java | interface | extends JpaRepository\<Student, Long\> |
| dto/CreateStudentRequest.java | record | record CreateStudentRequest(@NotBlank String name, @NotNull Integer grade) |
| dto/StudentResponse.java | record | record StudentResponse(Long id, String name, Integer grade) |
| domain/Problem.java | class | JPA 实体：@Entity @Table(name = "problems")；Long id (PK, IDENTITY), String concept (@NotBlank), String question (@NotBlank), String correctAnswer (@NotBlank), Double tolerance (Nullable) |
| repository/ProblemRepository.java | interface | extends JpaRepository\<Problem, Long\>；方法：List\<Problem\> findByConcept(String concept) |
| dto/CreateProblemRequest.java | record | record CreateProblemRequest(@NotBlank String concept, @NotBlank String question, @NotBlank String correctAnswer, Double tolerance) |
| dto/ProblemResponse.java | record | record ProblemResponse(Long id, String concept, String question, String correctAnswer, Double tolerance) |
| domain/Submission.java | class | JPA 实体：@Entity @Table(name = "submissions")；Long id (PK, IDENTITY), Long studentId (@NotNull), Long problemId (@NotNull), String answer (@NotBlank), boolean isCorrect, Double score, Instant submittedAt |
| repository/SubmissionRepository.java | interface | extends JpaRepository\<Submission, Long\>；方法：List\<Submission\> findByStudentId(Long studentId) |
| dto/CreateSubmissionRequest.java | record | record CreateSubmissionRequest(@NotNull Long studentId, @NotNull Long problemId, @NotNull String answer) |
| dto/SubmissionResponse.java | record | record SubmissionResponse(Long id, Long studentId, Long problemId, String answer, boolean correct, Double score, String correctAnswer, Instant submittedAt) |
| dto/ConceptMasteryResponse.java | record | record ConceptMasteryResponse(String concept, long attempts, long correct, double mastery) |
| dto/ParentReportResponse.java | record | record ParentReportResponse(Long studentId, String studentName, long totalSubmissions, Instant mostRecentSubmissionTime, String weakestConcept, List\<ConceptMasteryResponse\> masteryPerConcept) |
| service/StudentService.java | class | 学生业务逻辑：createStudent, getAllStudents, getStudentById |
| service/ProblemService.java | class | 题目业务逻辑：createProblem, getProblems(String concept) |
| service/GradingService.java | class | 批改引擎与作答业务：public boolean checkAnswer(String submitted, String correct, Double tolerance)；public SubmissionResponse submitAnswer(CreateSubmissionRequest req) |
| service/MasteryReportService.java | class | 掌握度与家长报告统计：public List\<ConceptMasteryResponse\> getStudentMastery(Long studentId)；public ParentReportResponse getParentReport(Long studentId) |
| controller/StudentController.java | class | REST 控制器：POST /students, GET /students |
| controller/ProblemController.java | class | REST 控制器：POST /problems, GET /problems |
| controller/SubmissionController.java | class | REST 控制器：POST /submissions |
| controller/ReportController.java | class | REST 控制器：GET /students/{id}/mastery, GET /students/{id}/report |
| config/DataInitializer.java | class | @Component CommandLineRunner，启动注入种子数据 |

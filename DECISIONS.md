# DECISIONS — Mock Challenge 1 预演

## D-001 模拟题范围（2026-11-08，Sam 拍板）
保持模拟题 1 不变：文本答案 + 规则判分，先把三方流程完整跑一遍。Sam 提出的真实产品形态（打印作业 PDF + 手写答案扫描 + AI 读题判分写评语，输出给学生/家长/老师）留作模拟题 2 的方向，本次不实现。

## D-002 DTO 组织方式冲突裁定（2026-11-08，项目经理 Gemini 裁定）
原 SPEC 第 3 节把 DTO 打包进 StudentDto/ProblemDto/SubmissionDto 等文件，与冻结测试 import 的独立顶级类不一致。裁定：**以测试为准**，DTO 全部拆为 dto 包下的独立顶级类（CreateStudentRequest、StudentResponse、CreateProblemRequest、ProblemResponse、CreateSubmissionRequest、SubmissionResponse、ConceptMasteryResponse、ParentReportResponse、ErrorResponse），SPEC 第 3 节已按此修订。测试本身不改动。

## D-003 种子数据顺序约束（2026-11-08，秘书备注）
控制器冻结测试假定种子数据插入顺序固定（Alice 的 id=1；第一道题 "Solve for x: 2x = 6" 的 id=1）。DataInitializer（T6）必须严格按 SPEC 2.6 的顺序插入，不得重排。

<!-- 程序员每完成一项任务，在此追加一条：日期、任务号、做了什么决定、有什么取舍或意外。 -->

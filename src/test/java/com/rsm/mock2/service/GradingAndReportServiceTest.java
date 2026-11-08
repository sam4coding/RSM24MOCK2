package com.rsm.mock2.service;

import com.rsm.mock2.domain.Problem;
import com.rsm.mock2.domain.Student;
import com.rsm.mock2.domain.Submission;
import com.rsm.mock2.dto.ConceptMasteryResponse;
import com.rsm.mock2.dto.CreateSubmissionRequest;
import com.rsm.mock2.dto.ParentReportResponse;
import com.rsm.mock2.dto.SubmissionResponse;
import com.rsm.mock2.exception.ResourceNotFoundException;
import com.rsm.mock2.repository.ProblemRepository;
import com.rsm.mock2.repository.StudentRepository;
import com.rsm.mock2.repository.SubmissionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GradingAndReportServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private ProblemRepository problemRepository;

    @Mock
    private SubmissionRepository submissionRepository;

    @InjectMocks
    private GradingService gradingService;

    @InjectMocks
    private MasteryReportService masteryReportService;

    @Test
    @DisplayName("批改规则1: 纯字符串不区分大小写且自动去除两端空白比较正确")
    void checkAnswer_caseInsensitiveAndTrim_shouldBeCorrect() {
        boolean correct = gradingService.checkAnswer("  one half  ", "One Half", null);
        assertTrue(correct, "Trim and case-insensitive string answers must evaluate to true");

        boolean incorrect = gradingService.checkAnswer("three", "four", null);
        assertFalse(incorrect, "Mismatched strings must evaluate to false");
    }

    @Test
    @DisplayName("批改规则2: 双数值在指定 tolerance 容差范围内判定为正确")
    void checkAnswer_numericWithinCustomTolerance_shouldBeCorrect() {
        // tolerance = 0.05, 3.03 - 3.00 = 0.03 <= 0.05
        boolean correct = gradingService.checkAnswer("3.03", "3.00", 0.05);
        assertTrue(correct, "Numeric answer within custom tolerance must evaluate to true");

        // 3.08 - 3.00 = 0.08 > 0.05
        boolean incorrect = gradingService.checkAnswer("3.08", "3.00", 0.05);
        assertFalse(incorrect, "Numeric answer exceeding custom tolerance must evaluate to false");
    }

    @Test
    @DisplayName("批改规则3: 题目未显式指定 tolerance 时，默认采用 0.01 容差")
    void checkAnswer_numericWithDefaultTolerance_shouldBeApplied() {
        // tolerance = null -> default 0.01; 0.755 - 0.75 = 0.005 <= 0.01
        boolean correct = gradingService.checkAnswer("0.755", "0.75", null);
        assertTrue(correct, "Answer within default 0.01 tolerance must evaluate to true");

        // 0.77 - 0.75 = 0.02 > 0.01
        boolean incorrect = gradingService.checkAnswer("0.77", "0.75", null);
        assertFalse(incorrect, "Answer exceeding default 0.01 tolerance must evaluate to false");
    }

    @Test
    @DisplayName("提交作答: 学生不存在时必须抛出 ResourceNotFoundException")
    void submitAnswer_studentNotFound_shouldThrowResourceNotFoundException() {
        when(studentRepository.findById(99L)).thenReturn(Optional.empty());

        CreateSubmissionRequest req = new CreateSubmissionRequest(99L, 1L, "42");
        assertThrows(ResourceNotFoundException.class, () -> gradingService.submitAnswer(req));
        verify(submissionRepository, never()).save(any());
    }

    @Test
    @DisplayName("提交作答: 题目不存在时必须抛出 ResourceNotFoundException")
    void submitAnswer_problemNotFound_shouldThrowResourceNotFoundException() {
        Student student = new Student();
        student.setId(1L);
        student.setName("Alice");
        student.setGrade(6);

        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(problemRepository.findById(88L)).thenReturn(Optional.empty());

        CreateSubmissionRequest req = new CreateSubmissionRequest(1L, 88L, "42");
        assertThrows(ResourceNotFoundException.class, () -> gradingService.submitAnswer(req));
        verify(submissionRepository, never()).save(any());
    }

    @Test
    @DisplayName("提交作答: 成功批改后返回 correct, score(1.0) 以及题目原始 correctAnswer")
    void submitAnswer_success_shouldReturnCorrectResponse() {
        Student student = new Student();
        student.setId(1L);
        student.setName("Alice");

        Problem problem = new Problem();
        problem.setId(2L);
        problem.setConcept("linear-equations");
        problem.setQuestion("2x = 6");
        problem.setCorrectAnswer("3");
        problem.setTolerance(0.01);

        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(problemRepository.findById(2L)).thenReturn(Optional.of(problem));
        when(submissionRepository.save(any(Submission.class))).thenAnswer(invocation -> {
            Submission sub = invocation.getArgument(0);
            sub.setId(100L);
            return sub;
        });

        CreateSubmissionRequest req = new CreateSubmissionRequest(1L, 2L, " 3.005 ");
        SubmissionResponse response = gradingService.submitAnswer(req);

        assertNotNull(response);
        assertEquals(100L, response.id());
        assertTrue(response.correct());
        assertEquals(1.0, response.score());
        assertEquals("3", response.correctAnswer());
        verify(submissionRepository, times(1)).save(any(Submission.class));
    }

    @Test
    @DisplayName("概念掌握度计算: 正确聚合各概念作答次数、正确数与正确率")
    void getStudentMastery_shouldCalculateAttemptsAndMasteryAccurately() {
        Student student = new Student();
        student.setId(1L);
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));

        Problem p1 = new Problem();
        p1.setId(10L);
        p1.setConcept("fractions");

        Problem p2 = new Problem();
        p2.setId(20L);
        p2.setConcept("linear-equations");

        when(problemRepository.findAll()).thenReturn(List.of(p1, p2));

        Submission s1 = new Submission();
        s1.setStudentId(1L);
        s1.setProblemId(10L);
        s1.setCorrect(true);

        Submission s2 = new Submission();
        s2.setStudentId(1L);
        s2.setProblemId(10L);
        s2.setCorrect(false);

        Submission s3 = new Submission();
        s3.setStudentId(1L);
        s3.setProblemId(20L);
        s3.setCorrect(true);

        when(submissionRepository.findByStudentId(1L)).thenReturn(List.of(s1, s2, s3));

        List<ConceptMasteryResponse> masteryList = masteryReportService.getStudentMastery(1L);

        assertEquals(2, masteryList.size());

        ConceptMasteryResponse fractions = masteryList.stream()
                .filter(m -> "fractions".equals(m.concept()))
                .findFirst().orElseThrow();
        assertEquals(2, fractions.attempts());
        assertEquals(1, fractions.correct());
        assertEquals(0.5, fractions.mastery(), 0.0001);

        ConceptMasteryResponse linear = masteryList.stream()
                .filter(m -> "linear-equations".equals(m.concept()))
                .findFirst().orElseThrow();
        assertEquals(1, linear.attempts());
        assertEquals(1, linear.correct());
        assertEquals(1.0, linear.mastery(), 0.0001);
    }

    @Test
    @DisplayName("家长报告生成: 正确找出 attempts >= 1 且 mastery 最低的最薄弱概念")
    void getParentReport_shouldIdentifyWeakestConcept() {
        Student student = new Student();
        student.setId(1L);
        student.setName("Alice");
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));

        Problem p1 = new Problem();
        p1.setId(10L);
        p1.setConcept("fractions");

        Problem p2 = new Problem();
        p2.setId(20L);
        p2.setConcept("linear-equations");

        when(problemRepository.findAll()).thenReturn(List.of(p1, p2));

        Instant t1 = Instant.parse("2026-11-08T09:00:00Z");
        Instant t2 = Instant.parse("2026-11-08T09:15:00Z");

        Submission s1 = new Submission();
        s1.setStudentId(1L);
        s1.setProblemId(10L);
        s1.setCorrect(false);
        s1.setSubmittedAt(t1);

        Submission s2 = new Submission();
        s2.setStudentId(1L);
        s2.setProblemId(20L);
        s2.setCorrect(true);
        s2.setSubmittedAt(t2);

        when(submissionRepository.findByStudentId(1L)).thenReturn(List.of(s1, s2));

        ParentReportResponse report = masteryReportService.getParentReport(1L);

        assertNotNull(report);
        assertEquals("Alice", report.studentName());
        assertEquals(2, report.totalSubmissions());
        assertEquals(t2, report.mostRecentSubmissionTime());
        assertEquals("fractions", report.weakestConcept(), "Fractions has 0.0 mastery and must be weakest concept");
    }
}

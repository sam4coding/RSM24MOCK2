package com.rsm.mock2.service;

import com.rsm.mock2.domain.Problem;
import com.rsm.mock2.domain.Student;
import com.rsm.mock2.domain.Submission;
import com.rsm.mock2.dto.CreateSubmissionRequest;
import com.rsm.mock2.dto.SubmissionResponse;
import com.rsm.mock2.exception.ResourceNotFoundException;
import com.rsm.mock2.repository.ProblemRepository;
import com.rsm.mock2.repository.StudentRepository;
import com.rsm.mock2.repository.SubmissionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@Transactional
public class GradingService {

    private final StudentRepository studentRepository;
    private final ProblemRepository problemRepository;
    private final SubmissionRepository submissionRepository;

    public GradingService(StudentRepository studentRepository,
                          ProblemRepository problemRepository,
                          SubmissionRepository submissionRepository) {
        this.studentRepository = studentRepository;
        this.problemRepository = problemRepository;
        this.submissionRepository = submissionRepository;
    }

    public boolean checkAnswer(String submitted, String correct, Double tolerance) {
        if (submitted == null || correct == null) {
            return false;
        }

        String s = submitted.trim();
        String c = correct.trim();

        // 规则 1: 忽略大小写的字符串直接比较
        if (s.equalsIgnoreCase(c)) {
            return true;
        }

        // 规则 2: 数值浮点数容差比较
        try {
            double numSubmitted = Double.parseDouble(s);
            double numCorrect = Double.parseDouble(c);
            double effTolerance = (tolerance != null) ? tolerance : 0.01;
            if (Math.abs(numSubmitted - numCorrect) <= effTolerance) {
                return true;
            }
        } catch (NumberFormatException ignored) {
            // 非数值格式，继续向下
        }

        return false;
    }

    public SubmissionResponse submitAnswer(CreateSubmissionRequest req) {
        Student student = studentRepository.findById(req.studentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student with id " + req.studentId() + " not found"));

        Problem problem = problemRepository.findById(req.problemId())
                .orElseThrow(() -> new ResourceNotFoundException("Problem with id " + req.problemId() + " not found"));

        boolean correct = checkAnswer(req.answer(), problem.getCorrectAnswer(), problem.getTolerance());
        Double score = correct ? 1.0 : 0.0;

        Submission submission = new Submission(
                student.getId(),
                problem.getId(),
                req.answer(),
                correct,
                score,
                Instant.now()
        );
        Submission saved = submissionRepository.save(submission);

        return new SubmissionResponse(
                saved.getId(),
                saved.getStudentId(),
                saved.getProblemId(),
                saved.getAnswer(),
                saved.isCorrect(),
                saved.getScore(),
                problem.getCorrectAnswer(),
                saved.getSubmittedAt()
        );
    }
}

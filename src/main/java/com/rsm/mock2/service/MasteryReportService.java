package com.rsm.mock2.service;

import com.rsm.mock2.domain.Problem;
import com.rsm.mock2.domain.Student;
import com.rsm.mock2.domain.Submission;
import com.rsm.mock2.dto.ConceptMasteryResponse;
import com.rsm.mock2.dto.ParentReportResponse;
import com.rsm.mock2.exception.ResourceNotFoundException;
import com.rsm.mock2.repository.ProblemRepository;
import com.rsm.mock2.repository.StudentRepository;
import com.rsm.mock2.repository.SubmissionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class MasteryReportService {

    private final StudentRepository studentRepository;
    private final ProblemRepository problemRepository;
    private final SubmissionRepository submissionRepository;

    public MasteryReportService(StudentRepository studentRepository,
                                ProblemRepository problemRepository,
                                SubmissionRepository submissionRepository) {
        this.studentRepository = studentRepository;
        this.problemRepository = problemRepository;
        this.submissionRepository = submissionRepository;
    }

    public List<ConceptMasteryResponse> getStudentMastery(Long studentId) {
        studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student with id " + studentId + " not found"));

        Map<Long, Problem> problemMap = problemRepository.findAll().stream()
                .filter(p -> p.getId() != null)
                .collect(Collectors.toMap(Problem::getId, p -> p, (a, b) -> a, LinkedHashMap::new));

        List<Submission> submissions = submissionRepository.findByStudentId(studentId);

        Map<String, long[]> conceptStats = new LinkedHashMap<>();
        for (Submission sub : submissions) {
            Problem problem = problemMap.get(sub.getProblemId());
            if (problem != null && problem.getConcept() != null) {
                String concept = problem.getConcept();
                long[] stats = conceptStats.computeIfAbsent(concept, k -> new long[2]);
                stats[0]++; // attempts
                if (sub.isCorrect()) {
                    stats[1]++; // correct
                }
            }
        }

        return conceptStats.entrySet().stream()
                .map(entry -> {
                    String concept = entry.getKey();
                    long attempts = entry.getValue()[0];
                    long correct = entry.getValue()[1];
                    double mastery = attempts > 0 ? (double) correct / attempts : 0.0;
                    return new ConceptMasteryResponse(concept, attempts, correct, mastery);
                })
                .toList();
    }

    public ParentReportResponse getParentReport(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student with id " + studentId + " not found"));

        List<Submission> submissions = submissionRepository.findByStudentId(studentId);
        long totalSubmissions = submissions.size();

        Instant mostRecentSubmissionTime = submissions.stream()
                .map(Submission::getSubmittedAt)
                .filter(Objects::nonNull)
                .max(Comparator.naturalOrder())
                .orElse(null);

        List<ConceptMasteryResponse> masteryList = getStudentMastery(studentId);

        String weakestConcept = masteryList.stream()
                .filter(m -> m.attempts() >= 1)
                .min(Comparator.comparingDouble(ConceptMasteryResponse::mastery))
                .map(ConceptMasteryResponse::concept)
                .orElse(null);

        return new ParentReportResponse(
                student.getId(),
                student.getName(),
                totalSubmissions,
                mostRecentSubmissionTime,
                weakestConcept,
                masteryList
        );
    }
}

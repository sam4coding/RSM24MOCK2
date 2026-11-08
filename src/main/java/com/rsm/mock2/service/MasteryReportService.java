package com.rsm.mock2.service;

import com.rsm.mock2.dto.ConceptMasteryResponse;
import com.rsm.mock2.dto.ParentReportResponse;
import com.rsm.mock2.repository.ProblemRepository;
import com.rsm.mock2.repository.StudentRepository;
import com.rsm.mock2.repository.SubmissionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
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
        return List.of();
    }

    public ParentReportResponse getParentReport(Long studentId) {
        return null;
    }
}

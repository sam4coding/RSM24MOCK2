package com.rsm.mock2.service;

import com.rsm.mock2.domain.Problem;
import com.rsm.mock2.dto.CreateProblemRequest;
import com.rsm.mock2.dto.ProblemResponse;
import com.rsm.mock2.exception.ResourceNotFoundException;
import com.rsm.mock2.repository.ProblemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ProblemService {

    private final ProblemRepository problemRepository;

    public ProblemService(ProblemRepository problemRepository) {
        this.problemRepository = problemRepository;
    }

    @Transactional
    public ProblemResponse createProblem(CreateProblemRequest request) {
        Problem problem = new Problem(
                request.concept().trim(),
                request.question().trim(),
                request.correctAnswer().trim(),
                request.tolerance()
        );
        Problem savedProblem = problemRepository.save(problem);
        return toResponse(savedProblem);
    }

    public List<ProblemResponse> getProblems(String concept) {
        List<Problem> problems;
        if (concept != null && !concept.isBlank()) {
            problems = problemRepository.findByConcept(concept.trim());
        } else {
            problems = problemRepository.findAll();
        }
        return problems.stream()
                .map(this::toResponse)
                .toList();
    }

    public Problem getProblemEntityById(Long id) {
        return problemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Problem with id " + id + " not found"));
    }

    public ProblemResponse getProblemById(Long id) {
        return toResponse(getProblemEntityById(id));
    }

    private ProblemResponse toResponse(Problem problem) {
        return new ProblemResponse(
                problem.getId(),
                problem.getConcept(),
                problem.getQuestion(),
                problem.getCorrectAnswer(),
                problem.getTolerance()
        );
    }
}

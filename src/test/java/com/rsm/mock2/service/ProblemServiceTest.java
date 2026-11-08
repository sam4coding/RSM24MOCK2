package com.rsm.mock2.service;

import com.rsm.mock2.domain.Problem;
import com.rsm.mock2.dto.CreateProblemRequest;
import com.rsm.mock2.dto.ProblemResponse;
import com.rsm.mock2.exception.ResourceNotFoundException;
import com.rsm.mock2.repository.ProblemRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProblemServiceTest {

    @Mock
    private ProblemRepository problemRepository;

    @InjectMocks
    private ProblemService problemService;

    @Test
    void testCreateProblem() {
        CreateProblemRequest req = new CreateProblemRequest("geometry", "Triangle angles sum?", "180", 0.0);
        Problem saved = new Problem(1L, "geometry", "Triangle angles sum?", "180", 0.0);
        when(problemRepository.save(any(Problem.class))).thenReturn(saved);

        ProblemResponse res = problemService.createProblem(req);

        assertNotNull(res);
        assertEquals(1L, res.id());
        assertEquals("geometry", res.concept());
        assertEquals("Triangle angles sum?", res.question());
        assertEquals("180", res.correctAnswer());
        assertEquals(0.0, res.tolerance());
        verify(problemRepository, times(1)).save(any(Problem.class));
    }

    @Test
    void testGetProblems_withoutConcept() {
        Problem p1 = new Problem(1L, "geometry", "Q1", "180", 0.0);
        Problem p2 = new Problem(2L, "algebra", "Q2", "3", 0.01);
        when(problemRepository.findAll()).thenReturn(List.of(p1, p2));

        List<ProblemResponse> list = problemService.getProblems(null);

        assertEquals(2, list.size());
        verify(problemRepository, times(1)).findAll();
        verify(problemRepository, never()).findByConcept(any());
    }

    @Test
    void testGetProblems_withConcept() {
        Problem p1 = new Problem(1L, "geometry", "Q1", "180", 0.0);
        when(problemRepository.findByConcept("geometry")).thenReturn(List.of(p1));

        List<ProblemResponse> list = problemService.getProblems(" geometry ");

        assertEquals(1, list.size());
        assertEquals("geometry", list.get(0).concept());
        verify(problemRepository, times(1)).findByConcept("geometry");
    }

    @Test
    void testGetProblemEntityById_found() {
        Problem p = new Problem(1L, "geometry", "Q1", "180", 0.0);
        when(problemRepository.findById(1L)).thenReturn(Optional.of(p));

        Problem res = problemService.getProblemEntityById(1L);

        assertNotNull(res);
        assertEquals(1L, res.getId());
    }

    @Test
    void testGetProblemEntityById_notFound() {
        when(problemRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> problemService.getProblemEntityById(99L)
        );

        assertEquals("Problem with id 99 not found", ex.getMessage());
    }
}

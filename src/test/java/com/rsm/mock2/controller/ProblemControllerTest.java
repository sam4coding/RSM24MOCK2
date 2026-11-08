package com.rsm.mock2.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rsm.mock2.dto.CreateProblemRequest;
import com.rsm.mock2.dto.ProblemResponse;
import com.rsm.mock2.service.ProblemService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProblemController.class)
class ProblemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProblemService problemService;

    @Test
    void testCreateProblem_success() throws Exception {
        CreateProblemRequest req = new CreateProblemRequest("geometry", "Triangle angles sum?", "180", 0.0);
        ProblemResponse res = new ProblemResponse(1L, "geometry", "Triangle angles sum?", "180", 0.0);
        when(problemService.createProblem(any())).thenReturn(res);

        mockMvc.perform(post("/problems")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.concept").value("geometry"))
                .andExpect(jsonPath("$.question").value("Triangle angles sum?"))
                .andExpect(jsonPath("$.correctAnswer").value("180"))
                .andExpect(jsonPath("$.tolerance").value(0.0));
    }

    @Test
    void testGetProblems_all() throws Exception {
        ProblemResponse p1 = new ProblemResponse(1L, "geometry", "Q1", "180", 0.0);
        ProblemResponse p2 = new ProblemResponse(2L, "algebra", "Q2", "3", 0.01);
        when(problemService.getProblems(null)).thenReturn(List.of(p1, p2));

        mockMvc.perform(get("/problems"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].concept").value("geometry"))
                .andExpect(jsonPath("$[1].concept").value("algebra"));
    }

    @Test
    void testGetProblems_byConcept() throws Exception {
        ProblemResponse p1 = new ProblemResponse(1L, "geometry", "Q1", "180", 0.0);
        when(problemService.getProblems(eq("geometry"))).thenReturn(List.of(p1));

        mockMvc.perform(get("/problems").param("concept", "geometry"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].concept").value("geometry"));
    }
}

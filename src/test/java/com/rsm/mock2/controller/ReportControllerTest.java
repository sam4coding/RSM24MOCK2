package com.rsm.mock2.controller;

import com.rsm.mock2.dto.ConceptMasteryResponse;
import com.rsm.mock2.dto.ParentReportResponse;
import com.rsm.mock2.exception.ResourceNotFoundException;
import com.rsm.mock2.service.MasteryReportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReportController.class)
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MasteryReportService masteryReportService;

    @Test
    void testGetStudentMastery_success() throws Exception {
        ConceptMasteryResponse c1 = new ConceptMasteryResponse("fractions", 2, 1, 0.5);
        when(masteryReportService.getStudentMastery(1L)).thenReturn(List.of(c1));

        mockMvc.perform(get("/students/1/mastery"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].concept").value("fractions"))
                .andExpect(jsonPath("$[0].attempts").value(2))
                .andExpect(jsonPath("$[0].correct").value(1))
                .andExpect(jsonPath("$[0].mastery").value(0.5));
    }

    @Test
    void testGetStudentMastery_notFound() throws Exception {
        when(masteryReportService.getStudentMastery(99L))
                .thenThrow(new ResourceNotFoundException("Student with id 99 not found"));

        mockMvc.perform(get("/students/99/mastery"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Student with id 99 not found"));
    }

    @Test
    void testGetParentReport_success() throws Exception {
        ParentReportResponse report = new ParentReportResponse(
                1L, "Alice", 2, Instant.parse("2026-11-08T09:15:00Z"), "fractions", List.of()
        );
        when(masteryReportService.getParentReport(1L)).thenReturn(report);

        mockMvc.perform(get("/students/1/report"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studentId").value(1))
                .andExpect(jsonPath("$.studentName").value("Alice"))
                .andExpect(jsonPath("$.totalSubmissions").value(2))
                .andExpect(jsonPath("$.weakestConcept").value("fractions"));
    }

    @Test
    void testGetParentReport_notFound() throws Exception {
        when(masteryReportService.getParentReport(99L))
                .thenThrow(new ResourceNotFoundException("Student with id 99 not found"));

        mockMvc.perform(get("/students/99/report"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Student with id 99 not found"));
    }
}

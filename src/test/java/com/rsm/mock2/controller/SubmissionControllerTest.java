package com.rsm.mock2.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rsm.mock2.dto.CreateSubmissionRequest;
import com.rsm.mock2.dto.SubmissionResponse;
import com.rsm.mock2.service.GradingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SubmissionController.class)
class SubmissionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private GradingService gradingService;

    @Test
    void testSubmitAnswer_success() throws Exception {
        CreateSubmissionRequest req = new CreateSubmissionRequest(1L, 2L, "3.0");
        SubmissionResponse res = new SubmissionResponse(
                10L, 1L, 2L, "3.0", true, 1.0, "3", Instant.now()
        );
        when(gradingService.submitAnswer(any())).thenReturn(res);

        mockMvc.perform(post("/submissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.studentId").value(1))
                .andExpect(jsonPath("$.problemId").value(2))
                .andExpect(jsonPath("$.correct").value(true))
                .andExpect(jsonPath("$.score").value(1.0))
                .andExpect(jsonPath("$.correctAnswer").value("3"));
    }
}

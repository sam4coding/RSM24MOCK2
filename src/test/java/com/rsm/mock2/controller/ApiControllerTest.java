package com.rsm.mock2.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rsm.mock2.dto.CreateProblemRequest;
import com.rsm.mock2.dto.CreateStudentRequest;
import com.rsm.mock2.dto.CreateSubmissionRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("POST /students 正常创建学生返回 201 或 200")
    void createStudent_validRequest_returnsStudent() throws Exception {
        CreateStudentRequest req = new CreateStudentRequest("David", 5);

        mockMvc.perform(post("/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("David"))
                .andExpect(jsonPath("$.grade").value(5));
    }

    @Test
    @DisplayName("POST /students 参数校验失败返回 400 与规范统一错误响应五字段")
    void createStudent_blankName_returns400WithUnifiedErrorFormat() throws Exception {
        CreateStudentRequest invalidReq = new CreateStudentRequest("   ", 6);

        mockMvc.perform(post("/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.path").value("/students"));
    }

    @Test
    @DisplayName("GET /problems 支持按 ?concept= 概念过滤题目")
    void getProblems_conceptFilter_returnsFilteredList() throws Exception {
        // 先插入一道几何题
        CreateProblemRequest geomReq = new CreateProblemRequest("geometry", "Triangle angles sum?", "180", 0.0);
        mockMvc.perform(post("/problems")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(geomReq)))
                .andExpect(status().is2xxSuccessful());

        mockMvc.perform(get("/problems")
                        .param("concept", "geometry"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].concept").value("geometry"));
    }

    @Test
    @DisplayName("POST /submissions 提交不存在的 studentId 返回 404 与统一错误响应")
    void submitAnswer_nonExistentStudent_returns404UnifiedError() throws Exception {
        CreateSubmissionRequest req = new CreateSubmissionRequest(999999L, 1L, "3");

        mockMvc.perform(post("/submissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message", containsString("999999")))
                .andExpect(jsonPath("$.path").value("/submissions"));
    }

    @Test
    @DisplayName("POST /submissions 正常批改并返回 correct, score 与 correctAnswer")
    void submitAnswer_validSubmission_returnsGradingDetails() throws Exception {
        // 种子数据中 Alice ID 为 1，第 1 题 linear-equations 答案为 "3"
        CreateSubmissionRequest req = new CreateSubmissionRequest(1L, 1L, "3.004");

        mockMvc.perform(post("/submissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.correct").value(true))
                .andExpect(jsonPath("$.score").value(1.0))
                .andExpect(jsonPath("$.correctAnswer").value("3"));
    }

    @Test
    @DisplayName("GET /students/{id}/mastery 与 GET /students/{id}/report 接口联调验证")
    void getMasteryAndReport_returnsValidJsonStructure() throws Exception {
        // 验证 Alice (ID: 1) 的掌握度接口
        mockMvc.perform(get("/students/1/mastery"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        // 验证 Alice (ID: 1) 的家长报告接口
        mockMvc.perform(get("/students/1/report"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studentId").value(1))
                .andExpect(jsonPath("$.studentName").value("Alice"))
                .andExpect(jsonPath("$.totalSubmissions").isNumber())
                .andExpect(jsonPath("$.masteryPerConcept").isArray());
    }
}

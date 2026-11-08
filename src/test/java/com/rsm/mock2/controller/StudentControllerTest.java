package com.rsm.mock2.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rsm.mock2.dto.CreateStudentRequest;
import com.rsm.mock2.dto.StudentResponse;
import com.rsm.mock2.service.StudentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StudentController.class)
class StudentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private StudentService studentService;

    @Test
    void testCreateStudent_success() throws Exception {
        CreateStudentRequest req = new CreateStudentRequest("Alice", 6);
        StudentResponse res = new StudentResponse(1L, "Alice", 6);
        when(studentService.createStudent(any())).thenReturn(res);

        mockMvc.perform(post("/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Alice"))
                .andExpect(jsonPath("$.grade").value(6));
    }

    @Test
    void testGetAllStudents() throws Exception {
        StudentResponse s1 = new StudentResponse(1L, "Alice", 6);
        StudentResponse s2 = new StudentResponse(2L, "Bob", 7);
        when(studentService.getAllStudents()).thenReturn(List.of(s1, s2));

        mockMvc.perform(get("/students"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name").value("Alice"))
                .andExpect(jsonPath("$[1].name").value("Bob"));
    }

    @Test
    void testGetStudentById() throws Exception {
        StudentResponse s1 = new StudentResponse(1L, "Alice", 6);
        when(studentService.getStudentById(1L)).thenReturn(s1);

        mockMvc.perform(get("/students/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Alice"))
                .andExpect(jsonPath("$.grade").value(6));
    }
}

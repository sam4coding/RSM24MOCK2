package com.rsm.mock2.service;

import com.rsm.mock2.domain.Student;
import com.rsm.mock2.dto.CreateStudentRequest;
import com.rsm.mock2.dto.StudentResponse;
import com.rsm.mock2.exception.ResourceNotFoundException;
import com.rsm.mock2.repository.StudentRepository;
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
class StudentServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @InjectMocks
    private StudentService studentService;

    @Test
    void testCreateStudent() {
        CreateStudentRequest req = new CreateStudentRequest("Alice", 6);
        Student savedStudent = new Student(1L, "Alice", 6);
        when(studentRepository.save(any(Student.class))).thenReturn(savedStudent);

        StudentResponse response = studentService.createStudent(req);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals("Alice", response.name());
        assertEquals(6, response.grade());
        verify(studentRepository, times(1)).save(any(Student.class));
    }

    @Test
    void testGetAllStudents() {
        Student s1 = new Student(1L, "Alice", 6);
        Student s2 = new Student(2L, "Bob", 7);
        when(studentRepository.findAll()).thenReturn(List.of(s1, s2));

        List<StudentResponse> list = studentService.getAllStudents();

        assertEquals(2, list.size());
        assertEquals("Alice", list.get(0).name());
        assertEquals("Bob", list.get(1).name());
    }

    @Test
    void testGetStudentEntityById_found() {
        Student s = new Student(1L, "Alice", 6);
        when(studentRepository.findById(1L)).thenReturn(Optional.of(s));

        Student result = studentService.getStudentEntityById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Alice", result.getName());
    }

    @Test
    void testGetStudentEntityById_notFound() {
        when(studentRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> studentService.getStudentEntityById(99L)
        );

        assertEquals("Student with id 99 not found", ex.getMessage());
    }
}

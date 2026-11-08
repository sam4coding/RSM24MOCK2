package com.rsm.mock2.service;

import com.rsm.mock2.domain.Student;
import com.rsm.mock2.dto.CreateStudentRequest;
import com.rsm.mock2.dto.StudentResponse;
import com.rsm.mock2.exception.ResourceNotFoundException;
import com.rsm.mock2.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class StudentService {

    private final StudentRepository studentRepository;

    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    @Transactional
    public StudentResponse createStudent(CreateStudentRequest request) {
        Student student = new Student(request.name(), request.grade());
        Student savedStudent = studentRepository.save(student);
        return toResponse(savedStudent);
    }

    public List<StudentResponse> getAllStudents() {
        return studentRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public Student getStudentEntityById(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student with id " + id + " not found"));
    }

    public StudentResponse getStudentById(Long id) {
        return toResponse(getStudentEntityById(id));
    }

    private StudentResponse toResponse(Student student) {
        return new StudentResponse(student.getId(), student.getName(), student.getGrade());
    }
}

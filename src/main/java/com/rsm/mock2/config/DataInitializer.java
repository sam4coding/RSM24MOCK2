package com.rsm.mock2.config;

import com.rsm.mock2.domain.Problem;
import com.rsm.mock2.domain.Student;
import com.rsm.mock2.repository.ProblemRepository;
import com.rsm.mock2.repository.StudentRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final StudentRepository studentRepository;
    private final ProblemRepository problemRepository;

    public DataInitializer(StudentRepository studentRepository, ProblemRepository problemRepository) {
        this.studentRepository = studentRepository;
        this.problemRepository = problemRepository;
    }

    @Override
    public void run(String... args) {
        if (studentRepository.count() == 0) {
            studentRepository.saveAll(List.of(
                    new Student("Alice", 6),
                    new Student("Bob", 7),
                    new Student("Charlie", 6)
            ));
        }

        if (problemRepository.count() == 0) {
            problemRepository.saveAll(List.of(
                    new Problem("linear-equations", "Solve for x: 2x = 6", "3", 0.01),
                    new Problem("linear-equations", "Solve for x: x + 5 = 12", "7", 0.01),
                    new Problem("linear-equations", "Solve for x: 3x - 1 = 8", "3", 0.01),
                    new Problem("fractions", "Simplify the fraction: 2/4", "1/2", null),
                    new Problem("fractions", "Calculate 1/2 + 1/4 as a decimal", "0.75", 0.01)
            ));
        }
    }
}

package com.rsm.mock2.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

@Entity
@Table(name = "submissions")
public class Submission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(nullable = false)
    private Long studentId;

    @NotNull
    @Column(nullable = false)
    private Long problemId;

    @NotBlank
    @Column(nullable = false)
    private String answer;

    @Column(nullable = false)
    private boolean isCorrect;

    @Column(nullable = false)
    private Double score;

    @Column(nullable = false)
    private Instant submittedAt = Instant.now();

    public Submission() {
    }

    public Submission(Long studentId, Long problemId, String answer, boolean isCorrect, Double score, Instant submittedAt) {
        this.studentId = studentId;
        this.problemId = problemId;
        this.answer = answer;
        this.isCorrect = isCorrect;
        this.score = score;
        this.submittedAt = (submittedAt != null) ? submittedAt : Instant.now();
    }

    public Submission(Long id, Long studentId, Long problemId, String answer, boolean isCorrect, Double score, Instant submittedAt) {
        this.id = id;
        this.studentId = studentId;
        this.problemId = problemId;
        this.answer = answer;
        this.isCorrect = isCorrect;
        this.score = score;
        this.submittedAt = (submittedAt != null) ? submittedAt : Instant.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public Long getProblemId() {
        return problemId;
    }

    public void setProblemId(Long problemId) {
        this.problemId = problemId;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public boolean isCorrect() {
        return isCorrect;
    }

    public void setCorrect(boolean correct) {
        isCorrect = correct;
    }

    public void setIsCorrect(boolean correct) {
        isCorrect = correct;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public Instant getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(Instant submittedAt) {
        this.submittedAt = submittedAt;
    }
}

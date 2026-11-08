package com.rsm.mock2.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "problems")
public class Problem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String concept;

    @NotBlank
    @Column(nullable = false, length = 1000)
    private String question;

    @NotBlank
    @Column(nullable = false)
    private String correctAnswer;

    @Column(nullable = true)
    private Double tolerance;

    public Problem() {
    }

    public Problem(String concept, String question, String correctAnswer, Double tolerance) {
        this.concept = concept;
        this.question = question;
        this.correctAnswer = correctAnswer;
        this.tolerance = tolerance;
    }

    public Problem(Long id, String concept, String question, String correctAnswer, Double tolerance) {
        this.id = id;
        this.concept = concept;
        this.question = question;
        this.correctAnswer = correctAnswer;
        this.tolerance = tolerance;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getConcept() {
        return concept;
    }

    public void setConcept(String concept) {
        this.concept = concept;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
    }

    public Double getTolerance() {
        return tolerance;
    }

    public void setTolerance(Double tolerance) {
        this.tolerance = tolerance;
    }
}

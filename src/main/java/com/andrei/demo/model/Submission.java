package com.andrei.demo.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@Table(name = "submission")
public class Submission {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "code", nullable = false, columnDefinition = "TEXT")
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(name = "language", nullable = false)
    private Language language;

    @Enumerated(EnumType.STRING)
    @Column(name = "result", nullable = false)
    private SubmissionResult result;

    @Column(name = "submitted_at", updatable = false)
    private LocalDateTime submittedAt;


    @ManyToOne
    @JoinColumn(name = "person_id", nullable = false)
    private Person person;


    @ManyToOne
    @JoinColumn(name = "problem_id", nullable = false)
    private Problem problem;

}
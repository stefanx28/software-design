package com.andrei.demo.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Entity
@Data
@Table(name = "problem")
public class Problem {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty", nullable = false)
    private Difficulty difficulty;


    @OneToMany(mappedBy = "problem", cascade = CascadeType.ALL)
    private List<Submission> submissions;


    @ManyToMany(mappedBy = "solvedProblems")
    private List<Person> solvers;
}

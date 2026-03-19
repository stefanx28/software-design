package com.andrei.demo.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Entity
@Data
@Table(name = "person")
public class Person {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "password", nullable = false)
    private String password;

    private Integer age;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @OneToMany(mappedBy = "person", cascade = CascadeType.ALL)
    private List<Submission> submissions;

    @ManyToMany
    @JoinTable(
            name = "person_problem",
            joinColumns = @JoinColumn(name = "person_id"),
            inverseJoinColumns = @JoinColumn(name = "problem_id")
    )
    private List<Problem> solvedProblems;
}

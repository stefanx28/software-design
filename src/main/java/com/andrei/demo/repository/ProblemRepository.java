package com.andrei.demo.repository;

import com.andrei.demo.model.Difficulty;
import com.andrei.demo.model.Problem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProblemRepository extends JpaRepository<Problem, UUID> {


    Optional<Problem> findByTitle(String title);
    List<Problem> findByDifficulty(Difficulty difficulty);

    List<Problem> findByTitleContaining(String string);
}

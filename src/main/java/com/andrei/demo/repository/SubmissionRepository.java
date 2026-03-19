package com.andrei.demo.repository;

import com.andrei.demo.model.Submission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, UUID> {

    List<Submission> findByPersonId(UUID personId);

    List<Submission> findByProblemId(UUID problemId);

    List<Submission> findByPersonIdAndProblemId(UUID personId, UUID problemId);
}
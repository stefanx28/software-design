package com.andrei.demo.service;

import com.andrei.demo.config.ValidationException;
import com.andrei.demo.model.Person;
import com.andrei.demo.model.Problem;
import com.andrei.demo.model.Submission;
import com.andrei.demo.model.SubmissionCreateDTO;
import com.andrei.demo.model.SubmissionResult;
import com.andrei.demo.repository.PersonRepository;
import com.andrei.demo.repository.ProblemRepository;
import com.andrei.demo.repository.SubmissionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class SubmissionService {

    private final SubmissionRepository submissionRepository;//used to find person
    private final PersonRepository personRepository;
    private final ProblemRepository problemRepository;//used to find problem


    public List<Submission> getAll() {
        return submissionRepository.findAll();
    }

    public Submission getById(UUID id) {
        return submissionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Submission not found with id: " + id));
    }

    public List<Submission> getByPersonId(UUID personId) {
        if (!personRepository.existsById(personId)) {
            throw new EntityNotFoundException("Person not found with id: " + personId);
        }
        return submissionRepository.findByPersonId(personId);
    }

    public List<Submission> getByProblemId(UUID problemId) {
        if (!problemRepository.existsById(problemId)) {
            throw new EntityNotFoundException("Problem not found with id: " + problemId);
        }
        return submissionRepository.findByProblemId(problemId);
    }


    public Submission addSubmission(SubmissionCreateDTO dto) throws ValidationException {
        Person person = personRepository.findById(dto.getPersonId())
                .orElseThrow(() -> new EntityNotFoundException("Person not found with id: " + dto.getPersonId()));

        Problem problem = problemRepository.findById(dto.getProblemId())
                .orElseThrow(() -> new EntityNotFoundException("Problem not found with id: " + dto.getProblemId()));

        if (dto.getCode() == null || dto.getCode().isBlank()) {
            throw new ValidationException("Code cannot be empty");
        }

        Submission submission = new Submission();
        submission.setPerson(person);
        submission.setProblem(problem);
        submission.setCode(dto.getCode());
        submission.setLanguage(dto.getLanguage());
        submission.setResult(SubmissionResult.ACCEPTED);

        return submissionRepository.save(submission);
    }


    public Submission updateSubmission(UUID id, SubmissionCreateDTO dto) throws ValidationException {
        Submission existing = submissionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Submission not found with id: " + id));

        Person person = personRepository.findById(dto.getPersonId())
                .orElseThrow(() -> new EntityNotFoundException("Person not found with id: " + dto.getPersonId()));

        Problem problem = problemRepository.findById(dto.getProblemId())
                .orElseThrow(() -> new EntityNotFoundException("Problem not found with id: " + dto.getProblemId()));

        if (dto.getCode() == null || dto.getCode().isBlank()) {
            throw new ValidationException("Code cannot be empty");
        }

        existing.setPerson(person);
        existing.setProblem(problem);
        existing.setCode(dto.getCode());
        existing.setLanguage(dto.getLanguage());
        existing.setResult(dto.getResult());

        return submissionRepository.save(existing);
    }



    public void deleteSubmission(UUID id) {
        if (!submissionRepository.existsById(id)) {
            throw new EntityNotFoundException("Submission not found with id: " + id);
        }
        submissionRepository.deleteById(id);
    }
}
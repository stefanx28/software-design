package com.andrei.demo.controller;

import com.andrei.demo.config.ValidationException;
import com.andrei.demo.model.Submission;
import com.andrei.demo.model.SubmissionCreateDTO;
import com.andrei.demo.model.SubmissionResponseDTO;
import com.andrei.demo.service.SubmissionService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@AllArgsConstructor
@CrossOrigin
@RequestMapping("/submission")
public class SubmissionController {

    private final SubmissionService submissionService;


    @GetMapping("/{id}")
    public Submission getById(@PathVariable UUID id) {
        return submissionService.getById(id);
    }

    @GetMapping("/person/{personId}")
    public List<Submission> getByPersonId(@PathVariable UUID personId) {
        return submissionService.getByPersonId(personId);
    }

    @GetMapping("/problem/{problemId}")
    public List<Submission> getByProblemId(@PathVariable UUID problemId) {
        return submissionService.getByProblemId(problemId);
    }


    @PutMapping("/{id}")
    public Submission update(@PathVariable UUID id,
                             @Valid @RequestBody SubmissionCreateDTO dto) throws ValidationException {
        return submissionService.updateSubmission(id, dto);
    }

    @PatchMapping("/{id}")
    public Submission patch(@PathVariable UUID id,
                            @RequestBody SubmissionCreateDTO dto) throws ValidationException {
        return submissionService.patchSubmission(id, dto);
    }



    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {
        submissionService.deleteSubmission(id);
    }

    @GetMapping
    public List<SubmissionResponseDTO> getAll() {
        return submissionService.getAll()
                .stream()
                .map(submissionService::toDTO)
                .collect(Collectors.toList());
    }

    @PostMapping
    public SubmissionResponseDTO create(@Valid @RequestBody SubmissionCreateDTO dto)
            throws ValidationException {
        return submissionService.toDTO(submissionService.addSubmission(dto));
    }

}
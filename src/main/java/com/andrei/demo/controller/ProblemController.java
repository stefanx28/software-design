package com.andrei.demo.controller;

import com.andrei.demo.config.ValidationException;
import com.andrei.demo.model.Problem;
import com.andrei.demo.model.ProblemCreateDTO;
import com.andrei.demo.service.ProblemService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@AllArgsConstructor
@CrossOrigin
@RequestMapping("/api/problem")
public class ProblemController {

    private final ProblemService problemService;

    @GetMapping
    public List<Problem> getProblems() {
        return problemService.getProblems();
    }

    @GetMapping("/{uuid}")
    public Problem getProblemById(@PathVariable UUID uuid) {
        return problemService.getProblemById(uuid);
    }

    @GetMapping("/title/{title}")
    public Problem getProblemByTitle(@PathVariable String title) {
        return problemService.getProblemByName(title);
    }

    @PostMapping
    public Problem addProblem(@Valid @RequestBody ProblemCreateDTO problemDTO) throws ValidationException {
        return problemService.addProblem(problemDTO);
    }

    @PutMapping("/{uuid}")
    public Problem updateProblem(@PathVariable UUID uuid,
                                 @RequestBody Problem problem)
            throws com.andrei.demo.config.ValidationException {
        return problemService.updateProblem(uuid, problem);
    }


    @PatchMapping("/{uuid}")
    public Problem patchProblem(@PathVariable UUID uuid,
                                @RequestBody Problem problem)
            throws com.andrei.demo.config.ValidationException {
        return problemService.patchProblem(uuid, problem);
    }

    @DeleteMapping("/{uuid}")
    public void deleteProblem(@PathVariable UUID uuid) {
        problemService.deleteProblem(uuid);
    }


}
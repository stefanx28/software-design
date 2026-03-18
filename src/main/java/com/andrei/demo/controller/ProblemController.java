package com.andrei.demo.controller;


import com.andrei.demo.model.Problem;
import com.andrei.demo.model.ProblemCreateDTO;
import com.andrei.demo.service.ProblemService;
import jakarta.validation.Valid;
import jakarta.validation.ValidationException;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@AllArgsConstructor
@CrossOrigin
public class ProblemController {

    private final ProblemService problemService;

    @GetMapping("/problem")
    public List<Problem> getProblems() {return problemService.getProblems();}

    @GetMapping("problem/{uuid}")
    public Problem getProblemById(@PathVariable UUID uuid){
        return problemService.getProblemById(uuid);
    }

    @GetMapping("problem/title/{title}")
    public Problem getProblemByTitle(@PathVariable String title){
        return problemService.getProblemByName(title);
    }

    @PostMapping("/problem")
    public Problem addProblem(
            @Valid @RequestBody ProblemCreateDTO problemDTO
    ){
        return problemService.addProblem(problemDTO);
    }

    @PutMapping("problem/{uuid}")
    public Problem updateProblem(@PathVariable UUID uuid,
                                 @RequestBody Problem problem)
            throws com.andrei.demo.config.ValidationException {
        return problemService.updateProblem(uuid, problem);
    }

    @DeleteMapping("problem/{uuid}")
    public void deleteProblem(@PathVariable UUID uuid){problemService.deleteProblem(uuid);}


}

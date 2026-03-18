package com.andrei.demo.service;

import com.andrei.demo.config.ValidationException;
import com.andrei.demo.model.Difficulty;
import com.andrei.demo.model.Problem;
import com.andrei.demo.model.ProblemCreateDTO;
import com.andrei.demo.repository.ProblemRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class ProblemService {

    private final ProblemRepository problemRepository;

    public List<Problem> getProblems(){return problemRepository.findAll();}

    public Problem addProblem(ProblemCreateDTO problemDTO){
        Problem problem = new Problem();

        problem.setDescription(problemDTO.getDescription());
        problem.setDifficulty(problemDTO.getDifficulty());
        problem.setTitle(problemDTO.getTitle());

        return problemRepository.save(problem);
    }

    public Problem updateProblem(UUID uuid, Problem problem) throws ValidationException{
        Optional<Problem> problemOptional = problemRepository.findById(uuid);

        if(problemOptional.isEmpty()){
            throw new ValidationException("Problem with id " + uuid + "not found");
        }

        Problem existingProblem = problemOptional.get();

        existingProblem.setTitle(problem.getTitle());
        existingProblem.setDifficulty(problem.getDifficulty());
        existingProblem.setDescription(problem.getDescription());

        return problemRepository.save(existingProblem);
    }

    public void deleteProblem(UUID uuid){problemRepository.deleteById(uuid);}

    public List<Problem> findByDifficulty(Difficulty difficulty) throws ValidationException {
        if(difficulty == null){
            throw new ValidationException("difficulty cannot be null");
        }
        return problemRepository
                .findByDifficulty(difficulty);
    }

    public List<Problem> findByTitleContaining(String string) throws ValidationException {
        if (string == null || string.isBlank()) {
            throw new ValidationException("Search string cannot be empty");
        }
        return problemRepository.findByTitleContaining(string);
    }

    public Problem getProblemById(UUID uuid){
        return problemRepository.findById(uuid).orElseThrow(
                () -> new IllegalStateException("Problem with id " + uuid + "not found")
        );
    }

    public Problem getProblemByName(String name){
        return problemRepository.findByTitle(name).orElseThrow(
                () -> new IllegalStateException("Problem with title " + name + "not found")
        );
    }

}

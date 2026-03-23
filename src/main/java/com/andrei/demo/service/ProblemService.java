package com.andrei.demo.service;

import com.andrei.demo.config.ValidationException;
import com.andrei.demo.model.Difficulty;
import com.andrei.demo.model.Person;
import com.andrei.demo.model.Problem;
import com.andrei.demo.model.ProblemCreateDTO;
import com.andrei.demo.repository.PersonRepository;
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
    private final PersonRepository personRepository;

    public List<Problem> getProblems(){return problemRepository.findAll();}

    public Problem addProblem(ProblemCreateDTO problemDTO) throws ValidationException {
        validateProblem(problemDTO);
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

    public Problem patchProblem(UUID uuid, Problem partial) throws ValidationException {
        Problem existing = getProblemById(uuid);

        if (partial.getTitle() != null)
            existing.setTitle(partial.getTitle());

        if (partial.getDescription() != null)
            existing.setDescription(partial.getDescription());

        if (partial.getDifficulty() != null)
            existing.setDifficulty(partial.getDifficulty());

        return problemRepository.save(existing);
    }


    public void deleteProblem(UUID id) {
        Problem problem = getProblemById(id);


        for (Person person : problem.getSolvers()) {
            person.getSolvedProblems().remove(problem);
            personRepository.save(person);
        }

        problemRepository.delete(problem);
    }
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



    private void validateProblem(ProblemCreateDTO dto) throws ValidationException {


        if (dto.getTitle() == null || dto.getTitle().isBlank()) {
            throw new ValidationException("Title cannot be empty");
        }
        if (dto.getTitle().length() < 3 || dto.getTitle().length() > 100) {
            throw new ValidationException("Title must be between 3 and 100 characters");
        }

        if (dto.getDescription() == null || dto.getDescription().isBlank()) {
            throw new ValidationException("Description cannot be empty");
        }
        if (dto.getDescription().length() < 10) {
            throw new ValidationException("Description must be at least 10 characters");
        }

        if (dto.getDifficulty() == null) {
            throw new ValidationException("Difficulty cannot be null");
        }
        if (problemRepository.findByTitle(dto.getTitle()).isPresent()) {
            throw new ValidationException("Problem with title '" + dto.getTitle() + "' already exists");
        }
    }
}

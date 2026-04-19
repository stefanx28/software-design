package com.andrei.demo.service;

import com.andrei.demo.config.ValidationException;
import com.andrei.demo.model.Person;
import com.andrei.demo.model.Problem;
import com.andrei.demo.model.ProblemCreateDTO;
import com.andrei.demo.model.Difficulty;
import com.andrei.demo.repository.PersonRepository;
import com.andrei.demo.repository.ProblemRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProblemServiceTests {

    @Mock
    private ProblemRepository problemRepository;

    @Mock
    private PersonRepository personRepository;

    @InjectMocks
    private ProblemService problemService;

    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }


    @Test
    void testGetProblems() {
        // given:
        List<Problem> problems = List.of(new Problem(), new Problem());

        // when:
        when(problemRepository.findAll()).thenReturn(problems);
        List<Problem> result = problemService.getProblems();

        // then:
        assertEquals(2, result.size());
        verify(problemRepository, times(1)).findAll();
        assertEquals(problems, result);
    }


    @Test
    void testGetProblemById() {
        // given:
        UUID id = UUID.randomUUID();
        Problem problem = new Problem();
        problem.setId(id);
        problem.setTitle("Two Sum");

        // when:
        when(problemRepository.findById(id)).thenReturn(Optional.of(problem));
        Problem result = problemService.getProblemById(id);

        // then:
        assertEquals(problem, result);
        assertEquals("Two Sum", result.getTitle());
        verify(problemRepository, times(1)).findById(id);
    }


    @Test
    void testGetProblemByIdNotFound() {
        // given:
        UUID id = UUID.randomUUID();

        // when:
        when(problemRepository.findById(id)).thenReturn(Optional.empty());

        // then:
        assertThrows(EntityNotFoundException.class,
                () -> problemService.getProblemById(id));
        verify(problemRepository, times(1)).findById(id);
    }


    @Test
    void testAddProblem() throws ValidationException {
        // given:
        ProblemCreateDTO dto = new ProblemCreateDTO();
        dto.setTitle("Two Sum");
        dto.setDescription("Find two numbers that add up to target.");
        dto.setDifficulty(Difficulty.EASY);

        Problem savedProblem = new Problem();
        savedProblem.setId(UUID.randomUUID());
        savedProblem.setTitle("Two Sum");
        savedProblem.setDescription("Find two numbers that add up to target.");
        savedProblem.setDifficulty(Difficulty.EASY);

        // when:
        when(problemRepository.findByTitle("Two Sum")).thenReturn(Optional.empty());
        when(problemRepository.save(any())).thenReturn(savedProblem);
        Problem result = problemService.addProblem(dto);

        // then:
        assertEquals(savedProblem, result);
        assertNotNull(result.getId());
        assertEquals("Two Sum", result.getTitle());
        verify(problemRepository, times(1)).save(any());
    }


    @Test
    void testAddProblemDuplicateTitle() {
        // given:
        ProblemCreateDTO dto = new ProblemCreateDTO();
        dto.setTitle("Two Sum");
        dto.setDescription("Find two numbers that add up to target.");
        dto.setDifficulty(Difficulty.EASY);

        // when:
        when(problemRepository.findByTitle("Two Sum"))
                .thenReturn(Optional.of(new Problem()));

        // then:
        assertThrows(ValidationException.class,
                () -> problemService.addProblem(dto));
        verify(problemRepository, times(1)).findByTitle("Two Sum");
    }


    @Test
    void testAddProblemEmptyTitle() {
        // given:
        ProblemCreateDTO dto = new ProblemCreateDTO();
        dto.setTitle("");
        dto.setDescription("Find two numbers that add up to target.");
        dto.setDifficulty(Difficulty.EASY);

        // when + then:
        assertThrows(ValidationException.class,
                () -> problemService.addProblem(dto));
    }


    @Test
    void testAddProblemShortDescription() {
        // given:
        ProblemCreateDTO dto = new ProblemCreateDTO();
        dto.setTitle("Valid Title");
        dto.setDescription("Too short");
        dto.setDifficulty(Difficulty.EASY);

        // when + then:
        assertThrows(ValidationException.class,
                () -> problemService.addProblem(dto));
    }


    @Test
    void testUpdateProblem() throws ValidationException {
        // given:
        UUID id = UUID.randomUUID();
        Problem existing = new Problem();
        existing.setId(id);
        existing.setTitle("Two Sum");
        existing.setDescription("Old description for the problem.");
        existing.setDifficulty(Difficulty.EASY);

        Problem updated = new Problem();
        updated.setTitle("Two Sum Updated");
        updated.setDescription("Updated description for the problem.");
        updated.setDifficulty(Difficulty.MEDIUM);

        // when:
        when(problemRepository.findById(id)).thenReturn(Optional.of(existing));
        when(problemRepository.save(any())).thenReturn(updated);
        Problem result = problemService.updateProblem(id, updated);

        // then:
        assertEquals("Two Sum Updated", result.getTitle());
        assertEquals(Difficulty.MEDIUM, result.getDifficulty());
        verify(problemRepository, times(1)).findById(id);
        verify(problemRepository, times(1)).save(any());
    }


    @Test
    void testUpdateProblemNotFound() {
        // given:
        UUID id = UUID.randomUUID();
        Problem problem = new Problem();

        // when:
        when(problemRepository.findById(id)).thenReturn(Optional.empty());

        // then:
        assertThrows(EntityNotFoundException.class,
                () -> problemService.updateProblem(id, problem));
        verify(problemRepository, times(1)).findById(id);
    }

    @Test
    void testPatchProblem() throws ValidationException {
        // given:
        UUID id = UUID.randomUUID();
        Problem existing = new Problem();
        existing.setId(id);
        existing.setTitle("Two Sum");
        existing.setDescription("Original description for the problem.");
        existing.setDifficulty(Difficulty.EASY);

        Problem partial = new Problem();
        partial.setDifficulty(Difficulty.HARD);

        // when:
        when(problemRepository.findById(id)).thenReturn(Optional.of(existing));
        when(problemRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        Problem result = problemService.patchProblem(id, partial);

        // then:
        assertEquals("Two Sum", result.getTitle());                    // unchanged
        assertEquals("Original description for the problem.", result.getDescription()); // unchanged
        assertEquals(Difficulty.HARD, result.getDifficulty());         // updated
        verify(problemRepository, times(1)).findById(id);
        verify(problemRepository, times(1)).save(any());
    }


    @Test
    void testDeleteProblem() {
        // given:
        UUID id = UUID.randomUUID();

        Person person = new Person();
        person.setId(UUID.randomUUID());
        person.setSolvedProblems(new ArrayList<>());

        Problem problem = new Problem();
        problem.setId(id);
        problem.setSubmissions(new ArrayList<>());
        problem.setSolvers(new ArrayList<>(List.of(person)));
        person.getSolvedProblems().add(problem);

        // when:
        when(problemRepository.findById(id)).thenReturn(Optional.of(problem));
        doNothing().when(problemRepository).delete(problem);
        problemService.deleteProblem(id);

        // then:
        assertFalse(person.getSolvedProblems().contains(problem));
        verify(personRepository, times(1)).save(person);
        verify(problemRepository, times(1)).delete(problem);
    }


    @Test
    void testDeleteProblemNotFound() {
        // given:
        UUID id = UUID.randomUUID();

        // when:
        when(problemRepository.findById(id)).thenReturn(Optional.empty());

        // then:
        assertThrows(EntityNotFoundException.class,
                () -> problemService.deleteProblem(id));
        verify(problemRepository, times(1)).findById(id);
    }
}
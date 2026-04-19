package com.andrei.demo.service;

import com.andrei.demo.config.ValidationException;
import com.andrei.demo.model.*;
import com.andrei.demo.model.Difficulty;
import com.andrei.demo.model.Language;
import com.andrei.demo.model.SubmissionResult;
import com.andrei.demo.repository.PersonRepository;
import com.andrei.demo.repository.ProblemRepository;
import com.andrei.demo.repository.SubmissionRepository;
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

class SubmissionServiceTests {

    @Mock
    private SubmissionRepository submissionRepository;

    @Mock
    private PersonRepository personRepository;

    @Mock
    private ProblemRepository problemRepository;

    @InjectMocks
    private SubmissionService submissionService;

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
    void testGetAll() {
        // given:
        List<Submission> submissions = List.of(new Submission(), new Submission());

        // when:
        when(submissionRepository.findAll()).thenReturn(submissions);
        List<Submission> result = submissionService.getAll();

        // then:
        assertEquals(2, result.size());
        verify(submissionRepository, times(1)).findAll();
        assertEquals(submissions, result);
    }


    @Test
    void testGetById() {
        // given:
        UUID id = UUID.randomUUID();
        Submission submission = new Submission();
        submission.setId(id);

        // when:
        when(submissionRepository.findById(id)).thenReturn(Optional.of(submission));
        Submission result = submissionService.getById(id);

        // then:
        assertEquals(submission, result);
        verify(submissionRepository, times(1)).findById(id);
    }


    @Test
    void testGetByIdNotFound() {
        // given:
        UUID id = UUID.randomUUID();

        // when:
        when(submissionRepository.findById(id)).thenReturn(Optional.empty());

        // then:
        assertThrows(EntityNotFoundException.class,
                () -> submissionService.getById(id));
        verify(submissionRepository, times(1)).findById(id);
    }


    @Test
    void testAddSubmission() throws ValidationException {
        // given:
        Person person = new Person();
        person.setId(UUID.randomUUID());
        person.setSolvedProblems(new ArrayList<>());

        Problem problem = new Problem();
        problem.setId(UUID.randomUUID());

        SubmissionCreateDTO dto = new SubmissionCreateDTO();
        dto.setPersonId(person.getId());
        dto.setProblemId(problem.getId());
        dto.setCode("class Solution { public int[] twoSum(int[] n, int t) { return new int[]{0,1}; } }");
        dto.setLanguage(Language.JAVA);

        Submission savedSubmission = new Submission();
        savedSubmission.setId(UUID.randomUUID());
        savedSubmission.setPerson(person);
        savedSubmission.setProblem(problem);
        savedSubmission.setCode(dto.getCode());
        savedSubmission.setLanguage(Language.JAVA);
        savedSubmission.setResult(SubmissionResult.ACCEPTED);

        // when:
        when(personRepository.findById(person.getId())).thenReturn(Optional.of(person));
        when(problemRepository.findById(problem.getId())).thenReturn(Optional.of(problem));
        when(submissionRepository.save(any())).thenReturn(savedSubmission);
        Submission result = submissionService.addSubmission(dto);

        // then:
        assertEquals(savedSubmission, result);
        assertEquals(SubmissionResult.ACCEPTED, result.getResult());
        assertTrue(person.getSolvedProblems().contains(problem));
        verify(submissionRepository, times(1)).save(any());
        verify(personRepository, times(1)).save(person);
    }


    @Test
    void testAddSubmissionPersonNotFound() {
        // given:
        SubmissionCreateDTO dto = new SubmissionCreateDTO();
        dto.setPersonId(UUID.randomUUID());
        dto.setProblemId(UUID.randomUUID());
        dto.setCode("class Solution {}");
        dto.setLanguage(Language.JAVA);

        // when:
        when(personRepository.findById(any())).thenReturn(Optional.empty());

        // then:
        assertThrows(EntityNotFoundException.class,
                () -> submissionService.addSubmission(dto));
        verify(personRepository, times(1)).findById(any());
    }


    @Test
    void testAddSubmissionProblemNotFound() {
        // given:
        Person person = new Person();
        person.setId(UUID.randomUUID());

        SubmissionCreateDTO dto = new SubmissionCreateDTO();
        dto.setPersonId(person.getId());
        dto.setProblemId(UUID.randomUUID());
        dto.setCode("class Solution {}");
        dto.setLanguage(Language.JAVA);

        // when:
        when(personRepository.findById(person.getId())).thenReturn(Optional.of(person));
        when(problemRepository.findById(any())).thenReturn(Optional.empty());

        // then:
        assertThrows(EntityNotFoundException.class,
                () -> submissionService.addSubmission(dto));
        verify(problemRepository, times(1)).findById(any());
    }


    @Test
    void testAddSubmissionEmptyCode() {
        // given:
        Person person = new Person();
        person.setId(UUID.randomUUID());

        Problem problem = new Problem();
        problem.setId(UUID.randomUUID());

        SubmissionCreateDTO dto = new SubmissionCreateDTO();
        dto.setPersonId(person.getId());
        dto.setProblemId(problem.getId());
        dto.setCode("");
        dto.setLanguage(Language.JAVA);

        // when:
        when(personRepository.findById(any())).thenReturn(Optional.of(person));
        when(problemRepository.findById(any())).thenReturn(Optional.of(problem));

        // then:
        assertThrows(ValidationException.class,
                () -> submissionService.addSubmission(dto));
    }


    @Test
    void testAddSubmissionDoesNotDuplicateSolvedProblem() throws ValidationException {
        // given:
        Problem problem = new Problem();
        problem.setId(UUID.randomUUID());

        Person person = new Person();
        person.setId(UUID.randomUUID());
        person.setSolvedProblems(new ArrayList<>(List.of(problem)));

        SubmissionCreateDTO dto = new SubmissionCreateDTO();
        dto.setPersonId(person.getId());
        dto.setProblemId(problem.getId());
        dto.setCode("class Solution { public int[] twoSum(int[] n, int t) { return new int[]{0,1}; } }");
        dto.setLanguage(Language.JAVA);

        // when:
        when(personRepository.findById(person.getId())).thenReturn(Optional.of(person));
        when(problemRepository.findById(problem.getId())).thenReturn(Optional.of(problem));
        when(submissionRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        submissionService.addSubmission(dto);

        // then:
        assertEquals(1, person.getSolvedProblems().size());
        verify(personRepository, times(0)).save(person);
    }


    @Test
    void testUpdateSubmission() throws ValidationException {
        // given:
        UUID id = UUID.randomUUID();

        Person person = new Person();
        person.setId(UUID.randomUUID());

        Problem problem = new Problem();
        problem.setId(UUID.randomUUID());

        Submission existing = new Submission();
        existing.setId(id);
        existing.setPerson(person);
        existing.setProblem(problem);
        existing.setCode("old code here for testing purposes");
        existing.setLanguage(Language.JAVA);
        existing.setResult(SubmissionResult.ACCEPTED);

        SubmissionCreateDTO dto = new SubmissionCreateDTO();
        dto.setPersonId(person.getId());
        dto.setProblemId(problem.getId());
        dto.setCode("new updated code here for testing");
        dto.setLanguage(Language.PYTHON);
        dto.setResult(SubmissionResult.WRONG_ANSWER);

        Submission updatedSubmission = new Submission();
        updatedSubmission.setId(id);
        updatedSubmission.setPerson(person);
        updatedSubmission.setProblem(problem);
        updatedSubmission.setCode(dto.getCode());
        updatedSubmission.setLanguage(Language.PYTHON);
        updatedSubmission.setResult(SubmissionResult.WRONG_ANSWER);

        // when:
        when(submissionRepository.findById(id)).thenReturn(Optional.of(existing));
        when(personRepository.findById(person.getId())).thenReturn(Optional.of(person));
        when(problemRepository.findById(problem.getId())).thenReturn(Optional.of(problem));
        when(submissionRepository.save(any())).thenReturn(updatedSubmission);
        Submission result = submissionService.updateSubmission(id, dto);

        // then:
        assertEquals("new updated code here for testing", result.getCode());
        assertEquals(Language.PYTHON, result.getLanguage());
        assertEquals(SubmissionResult.WRONG_ANSWER, result.getResult());
        verify(submissionRepository, times(1)).findById(id);
        verify(submissionRepository, times(1)).save(any());
    }

    @Test
    void testUpdateSubmissionNotFound() {
        // given:
        UUID id = UUID.randomUUID();
        SubmissionCreateDTO dto = new SubmissionCreateDTO();

        // when:
        when(submissionRepository.findById(id)).thenReturn(Optional.empty());

        // then:
        assertThrows(EntityNotFoundException.class,
                () -> submissionService.updateSubmission(id, dto));
        verify(submissionRepository, times(1)).findById(id);
    }


    @Test
    void testPatchSubmission() throws ValidationException {
        // given:
        UUID id = UUID.randomUUID();

        Person person = new Person();
        person.setId(UUID.randomUUID());

        Problem problem = new Problem();
        problem.setId(UUID.randomUUID());

        Submission existing = new Submission();
        existing.setId(id);
        existing.setPerson(person);
        existing.setProblem(problem);
        existing.setCode("original code here for testing");
        existing.setLanguage(Language.JAVA);
        existing.setResult(SubmissionResult.ACCEPTED);

        SubmissionCreateDTO partial = new SubmissionCreateDTO();
        partial.setResult(SubmissionResult.WRONG_ANSWER);

        // when:
        when(submissionRepository.findById(id)).thenReturn(Optional.of(existing));
        when(submissionRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        Submission result = submissionService.patchSubmission(id, partial);

        // then:
        assertEquals("original code here for testing", result.getCode());
        assertEquals(Language.JAVA, result.getLanguage());
        assertEquals(SubmissionResult.WRONG_ANSWER, result.getResult());
        verify(submissionRepository, times(1)).findById(id);
        verify(submissionRepository, times(1)).save(any());
    }


    @Test
    void testDeleteSubmission() {
        // given:
        UUID id = UUID.randomUUID();
        Submission submission = new Submission();
        submission.setId(id);

        // when:
        when(submissionRepository.findById(id)).thenReturn(Optional.of(submission));
        doNothing().when(submissionRepository).deleteById(id);
        submissionService.deleteSubmission(id);

        // then:
        verify(submissionRepository, times(1)).deleteById(id);
    }


    @Test
    void testDeleteSubmissionNotFound() {
        // given:
        UUID id = UUID.randomUUID();

        // when:
        when(submissionRepository.findById(id)).thenReturn(Optional.empty());

        // then:
        assertThrows(EntityNotFoundException.class,
                () -> submissionService.deleteSubmission(id));
        verify(submissionRepository, times(1)).findById(id);
    }


    @Test
    void testGetByPersonIdNotFound() {
        // given:
        UUID personId = UUID.randomUUID();

        // when:
        when(personRepository.existsById(personId)).thenReturn(false);

        // then:
        assertThrows(EntityNotFoundException.class,
                () -> submissionService.getByPersonId(personId));
        verify(personRepository, times(1)).existsById(personId);
    }


    @Test
    void testGetByProblemIdNotFound() {
        // given:
        UUID problemId = UUID.randomUUID();

        // when:
        when(problemRepository.existsById(problemId)).thenReturn(false);

        // then:
        assertThrows(EntityNotFoundException.class,
                () -> submissionService.getByProblemId(problemId));
        verify(problemRepository, times(1)).existsById(problemId);
    }
}
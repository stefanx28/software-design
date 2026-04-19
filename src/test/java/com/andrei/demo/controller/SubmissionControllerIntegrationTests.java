package com.andrei.demo.controller;

import com.andrei.demo.model.Person;
import com.andrei.demo.model.Problem;
import com.andrei.demo.model.Submission;
import com.andrei.demo.model.Difficulty;
import com.andrei.demo.model.Language;
import com.andrei.demo.model.SubmissionResult;
import com.andrei.demo.repository.PersonRepository;
import com.andrei.demo.repository.ProblemRepository;
import com.andrei.demo.repository.SubmissionRepository;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
public class SubmissionControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SubmissionRepository submissionRepository;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private ProblemRepository problemRepository;

    private static final String FIXTURE_PATH = "src/test/resources/fixtures/";

    private Person savedPerson;
    private Problem savedProblem;

    @BeforeEach
    void setUp() {
        submissionRepository.deleteAll();
        submissionRepository.flush();
        personRepository.deleteAll();
        personRepository.flush();
        problemRepository.deleteAll();
        problemRepository.flush();
        seedDatabase();
    }

    private void seedDatabase() {
        Person person = new Person();
        person.setName("Andrei Pop");
        person.setEmail("andrei@test.com");
        person.setPassword("Andrei123!@#");
        person.setAge(20);
        person.setSolvedProblems(new ArrayList<>());
        person.setSubmissions(new ArrayList<>());
        savedPerson = personRepository.save(person);

        Problem problem = new Problem();
        problem.setTitle("Two Sum");
        problem.setDescription("Find two numbers that add up to target value.");
        problem.setDifficulty(Difficulty.EASY);
        problem.setSubmissions(new ArrayList<>());
        problem.setSolvers(new ArrayList<>());
        savedProblem = problemRepository.save(problem);

        Submission submission = new Submission();
        submission.setPerson(savedPerson);
        submission.setProblem(savedProblem);
        submission.setCode("class Solution { public int[] twoSum(int[] n, int t) { return new int[]{0,1}; } }");
        submission.setLanguage(Language.JAVA);
        submission.setResult(SubmissionResult.ACCEPTED);
        submissionRepository.save(submission);
    }


    @Test
    void testGetAll() throws Exception {
        mockMvc.perform(get("/api/submission"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].language").value("JAVA"))
                .andExpect(jsonPath("$[0].result").value("ACCEPTED"));
    }


    @Test
    void testGetById() throws Exception {
        Submission submission = submissionRepository.findAll().get(0);

        mockMvc.perform(get("/api/submission/{id}", submission.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(submission.getId().toString()))
                .andExpect(jsonPath("$.language").value("JAVA"))
                .andExpect(jsonPath("$.result").value("ACCEPTED"));
    }


    @Test
    void testGetByIdNotFound() throws Exception {
        mockMvc.perform(get("/api/submission/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value(Matchers.containsString("not found")));
    }


    @Test
    void testGetByPersonId() throws Exception {
        mockMvc.perform(get("/api/submission/person/{personId}",
                        savedPerson.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].result").value("ACCEPTED"));
    }


    @Test
    void testGetByPersonIdNotFound() throws Exception {
        mockMvc.perform(get("/api/submission/person/{personId}",
                        UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value(Matchers.containsString("not found")));
    }


    @Test
    void testGetByProblemId() throws Exception {
        mockMvc.perform(get("/api/submission/problem/{problemId}",
                        savedProblem.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].language").value("JAVA"));
    }


    @Test
    void testAddSubmission_ValidPayload() throws Exception {
        String validSubmissionJson = String.format("""
                {
                    "personId": "%s",
                    "problemId": "%s",
                    "code": "class Solution { public int[] twoSum(int[] n, int t) { return new int[]{0,1}; } }",
                    "language": "JAVA"
                }
                """, savedPerson.getId(), savedProblem.getId());

        mockMvc.perform(post("/api/submission")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validSubmissionJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.language").value("JAVA"))
                .andExpect(jsonPath("$.result").value("ACCEPTED"));
    }


    @Test
    void testAddSubmission_InvalidPayload() throws Exception {
        String invalidSubmissionJson = loadFixture("invalid_submission.json");

        mockMvc.perform(post("/api/submission")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidSubmissionJson))
                .andExpect(status().isBadRequest());
    }


    @Test
    void testAddSubmission_PersonNotFound() throws Exception {
        String submissionJson = String.format("""
                {
                    "personId": "%s",
                    "problemId": "%s",
                    "code": "class Solution { public int[] twoSum(int[] n, int t) { return new int[]{0,1}; } }",
                    "language": "JAVA"
                }
                """, UUID.randomUUID(), savedProblem.getId());

        mockMvc.perform(post("/api/submission")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(submissionJson))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value(Matchers.containsString("Person not found")));
    }


    @Test
    void testAddSubmission_ProblemNotFound() throws Exception {
        String submissionJson = String.format("""
                {
                    "personId": "%s",
                    "problemId": "%s",
                    "code": "class Solution { public int[] twoSum(int[] n, int t) { return new int[]{0,1}; } }",
                    "language": "JAVA"
                }
                """, savedPerson.getId(), UUID.randomUUID());

        mockMvc.perform(post("/api/submission")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(submissionJson))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value(Matchers.containsString("Problem not found")));
    }


    @Test
    void testAddSubmission_EmptyCode() throws Exception {
        String submissionJson = String.format("""
                {
                    "personId": "%s",
                    "problemId": "%s",
                    "code": "",
                    "language": "JAVA"
                }
                """, savedPerson.getId(), savedProblem.getId());

        mockMvc.perform(post("/api/submission")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(submissionJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value(Matchers.containsString("Code cannot be empty")));
    }


    @Test
    void testUpdateSubmission() throws Exception {
        Submission submission = submissionRepository.findAll().get(0);

        String updatedJson = String.format("""
                {
                    "personId": "%s",
                    "problemId": "%s",
                    "code": "class Solution { public int[] twoSum(int[] n, int t) { return new int[]{1,2}; } }",
                    "language": "PYTHON",
                    "result": "WRONG_ANSWER"
                }
                """, savedPerson.getId(), savedProblem.getId());

        mockMvc.perform(put("/api/submission/{id}", submission.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatedJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.language").value("PYTHON"))
                .andExpect(jsonPath("$.result").value("WRONG_ANSWER"));
    }


    @Test
    void testPatchSubmission() throws Exception {
        Submission submission = submissionRepository.findAll().get(0);

        String patchJson = """
                {
                    "result": "WRONG_ANSWER"
                }
                """;

        mockMvc.perform(patch("/api/submission/{id}", submission.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(patchJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("WRONG_ANSWER"))
                .andExpect(jsonPath("$.language").value("JAVA"));
    }


    @Test
    void testDeleteSubmission() throws Exception {
        Submission submission = submissionRepository.findAll().get(0);

        mockMvc.perform(delete("/api/submission/{id}", submission.getId()))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/submission/{id}", submission.getId()))
                .andExpect(status().isNotFound());
    }


    @Test
    void testDeleteSubmissionNotFound() throws Exception {
        mockMvc.perform(delete("/api/submission/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value(Matchers.containsString("not found")));
    }

    private String loadFixture(String fileName) throws IOException {
        return Files.readString(Paths.get(FIXTURE_PATH + fileName));
    }
}
package com.andrei.demo.controller;

import com.andrei.demo.model.Problem;
import com.andrei.demo.model.Difficulty;
import com.andrei.demo.repository.ProblemRepository;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
public class ProblemControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProblemRepository problemRepository;

    private static final String FIXTURE_PATH = "src/test/resources/fixtures/";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() throws Exception {
        problemRepository.deleteAll();
        problemRepository.flush();
        seedDatabase();
    }

    private void seedDatabase() throws Exception {
        String seedDataJson = loadFixture("problem_seed.json");
        List<Problem> problems = objectMapper.readValue(seedDataJson, new TypeReference<>() {});
        problemRepository.saveAll(problems);
    }


    @Test
    void testGetProblems() throws Exception {
        mockMvc.perform(get("/api/problem"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[*].title",
                        Matchers.containsInAnyOrder("Two Sum", "Reverse String")))
                .andExpect(jsonPath("$[*].difficulty",
                        Matchers.containsInAnyOrder("EASY", "EASY")));
    }


    @Test
    void testGetProblemById() throws Exception {
        Problem problem = problemRepository.findAll().get(0);

        mockMvc.perform(get("/api/problem/{uuid}", problem.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(problem.getId().toString()))
                .andExpect(jsonPath("$.title").value(problem.getTitle()));
    }


    @Test
    void testGetProblemByIdNotFound() throws Exception {
        mockMvc.perform(get("/api/problem/{uuid}", UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value(Matchers.containsString("not found")));
    }


    @Test
    void testGetProblemByTitle() throws Exception {
        mockMvc.perform(get("/api/problem/title/{title}", "Two Sum"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Two Sum"))
                .andExpect(jsonPath("$.difficulty").value("EASY"));
    }


    @Test
    void testAddProblem_ValidPayload() throws Exception {
        String validProblemJson = loadFixture("valid_problem.json");

        mockMvc.perform(post("/api/problem")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validProblemJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("Hanoi"))
                .andExpect(jsonPath("$.difficulty").value("MEDIUM"))
                .andExpect(jsonPath("$.description")
                        .value("Solve the towers of Hanoi problem recursively."));
    }


    @Test
    void testAddProblem_InvalidPayload() throws Exception {
        String invalidProblemJson = loadFixture("invalid_problem.json");

        mockMvc.perform(post("/api/problem")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidProblemJson))
                .andExpect(status().isBadRequest());
    }


    @Test
    void testAddProblem_DuplicateTitle() throws Exception {
        String validProblemJson = loadFixture("valid_problem.json");


        mockMvc.perform(post("/api/problem")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validProblemJson))
                .andExpect(status().isOk());


        mockMvc.perform(post("/api/problem")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validProblemJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value(Matchers.containsString("already exists")));
    }


    @Test
    void testUpdateProblem() throws Exception {
        Problem problem = problemRepository.findAll().get(0);

        String updatedJson = """
                {
                    "title": "Two Sum Updated",
                    "description": "Updated description for two sum problem.",
                    "difficulty": "MEDIUM"
                }
                """;

        mockMvc.perform(put("/api/problem/{uuid}", problem.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatedJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Two Sum Updated"))
                .andExpect(jsonPath("$.difficulty").value("MEDIUM"));
    }


    @Test
    void testUpdateProblemNotFound() throws Exception {
        String updatedJson = """
                {
                    "title": "Two Sum Updated",
                    "description": "Updated description for two sum problem.",
                    "difficulty": "MEDIUM"
                }
                """;

        mockMvc.perform(put("/api/problem/{uuid}", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatedJson))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value(Matchers.containsString("not found")));
    }


    @Test
    void testPatchProblem() throws Exception {
        Problem problem = problemRepository.findAll().get(0);

        String patchJson = """
                {
                    "difficulty": "HARD"
                }
                """;

        mockMvc.perform(patch("/api/problem/{uuid}", problem.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(patchJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.difficulty").value("HARD"))
                .andExpect(jsonPath("$.title").value(problem.getTitle()));
    }


    @Test
    void testDeleteProblem() throws Exception {
        Problem problem = problemRepository.findAll().get(0);

        mockMvc.perform(delete("/api/problem/{uuid}", problem.getId()))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/problem/{uuid}", problem.getId()))
                .andExpect(status().isNotFound());
    }


    @Test
    void testDeleteProblemNotFound() throws Exception {
        mockMvc.perform(delete("/api/problem/{uuid}", UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value(Matchers.containsString("not found")));
    }

    private String loadFixture(String fileName) throws IOException {
        return Files.readString(Paths.get(FIXTURE_PATH + fileName));
    }
}
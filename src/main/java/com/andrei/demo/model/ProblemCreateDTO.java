package com.andrei.demo.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProblemCreateDTO {

    @NotBlank(message = "title required")
    @Size(min = 2, max = 100, message =
            "Title should be between 2 and 100 characters")
    private String title;

    @NotBlank(message = "Problem body required")
    private String description;

    @NotNull(message = "difficulty required")
    private Difficulty difficulty;
}

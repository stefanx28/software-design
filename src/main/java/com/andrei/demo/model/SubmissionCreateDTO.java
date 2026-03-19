package com.andrei.demo.model;

import com.andrei.demo.model.Language;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.UUID;

@Data
public class SubmissionCreateDTO {

    @NotNull(message = "Person ID is required")
    private UUID personId;

    @NotNull(message = "Problem ID is required")
    private UUID problemId;

    @NotNull(message = "Code is required")
    private String code;

    @NotNull(message = "Language is required")
    private Language language;

    private SubmissionResult result;
}
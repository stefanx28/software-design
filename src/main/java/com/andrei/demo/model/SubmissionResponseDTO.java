package com.andrei.demo.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
public class SubmissionResponseDTO {
    private UUID id;
    private String code;
    private Language language;
    private SubmissionResult result;
    private LocalDateTime submittedAt;
    private UUID personId;
    private String personName;
    private UUID problemId;
    private String problemTitle;
}

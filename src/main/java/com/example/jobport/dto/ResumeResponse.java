package com.example.jobport.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ResumeResponse {
    private Long id;
    private Long userId;
    private String originalFileName;
    private String filePath;
    private String extractedText;
    private String extractedSkills;
    private LocalDateTime uploadedAt;
}
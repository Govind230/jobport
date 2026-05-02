package com.example.jobport.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class JobMatchResponse {
    private Long jobId;
    private String jobTitle;
    private Double score;
    private String matchedSkills;
}

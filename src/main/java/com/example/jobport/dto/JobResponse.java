package com.example.jobport.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class JobResponse {
    private Long id;
    private String title;
    private String description;
    private String skills;
    private String location;
    private Double salary;
    private Long recruiterId;
}

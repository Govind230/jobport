package com.example.jobport.service;

import com.example.jobport.dto.JobMatchResponse;
import com.example.jobport.entity.Job;
import com.example.jobport.entity.Resume;
import com.example.jobport.dao.JobRepository;
import com.example.jobport.dao.ResumeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

@Service
@RequiredArgsConstructor
public class MatchingService {

    private final ResumeRepository resumeRepository;
    private final JobRepository jobRepository;

    public Page<JobMatchResponse> matchJobsForResume(Long resumeId, Pageable pageable) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new RuntimeException("Resume not found: " + resumeId));

        Set<String> resumeSkills = splitSkills(resume.getExtractedSkills());

        // Fetch jobs page by page
        Page<Job> jobPage = jobRepository.findAll(pageable);

        List<JobMatchResponse> matches = jobPage.stream()
                .map(job -> calculateScore(resumeSkills, job))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .sorted(Comparator.comparing(JobMatchResponse::getScore).reversed())
                .collect(Collectors.toList());

        return new PageImpl<>(matches, pageable, jobPage.getTotalElements());
    }

    private Optional<JobMatchResponse> calculateScore(Set<String> resumeSkills, Job job) {
        Set<String> jobSkills = splitSkills(job.getSkills());
        Set<String> common = new HashSet<>(resumeSkills);
        common.retainAll(jobSkills);

        if (common.isEmpty()) return Optional.empty();

        double score = (double) common.size() / Math.max(jobSkills.size(), 1) * 100.0;
        return Optional.of(JobMatchResponse.builder()
                .jobId(job.getId())
                .jobTitle(job.getTitle())
                .score(Math.round(score * 100.0) / 100.0)
                .matchedSkills(String.join(", ", common))
                .build());
    }

    private Set<String> splitSkills(String skills) {
        if (skills == null || skills.isBlank()) return new HashSet<>();
        return Arrays.stream(skills.toLowerCase().split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .collect(Collectors.toSet());
    }
}
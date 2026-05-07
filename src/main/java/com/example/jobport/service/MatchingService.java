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

    private static final double SKILL_WEIGHT = 0.50;
    private static final double TEXT_WEIGHT = 0.35;
    private static final double TITLE_WEIGHT = 0.15;
    private static final Set<String> STOP_WORDS = Set.of(
            "and", "or", "but", "the", "a", "an", "to", "for", "with",
            "of", "in", "on", "by", "at", "is", "are", "as", "from", "that");

    private final ResumeRepository resumeRepository;
    private final JobRepository jobRepository;

    public Page<JobMatchResponse> matchJobsForResume(Long resumeId, Pageable pageable) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new RuntimeException("Resume not found: " + resumeId));

        Set<String> resumeSkills = splitSkills(resume.getExtractedSkills());
        Set<String> resumeTokens = extractTokens(safeText(resume.getExtractedText()));

        List<JobMatchResponse> matches = jobRepository.findAll().stream()
                .map(job -> calculateScore(resumeSkills, resumeTokens, job))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .sorted(Comparator.comparing(JobMatchResponse::getScore).reversed())
                .collect(Collectors.toList());

        int total = matches.size();
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), total);
        List<JobMatchResponse> pageContent = start <= end ? matches.subList(start, end) : Collections.emptyList();

        return new PageImpl<>(pageContent, pageable, total);
    }

    private Optional<JobMatchResponse> calculateScore(Set<String> resumeSkills, Set<String> resumeTokens, Job job) {
        Set<String> jobSkills = splitSkills(job.getSkills());
        Set<String> skillMatches = new HashSet<>(resumeSkills);
        skillMatches.retainAll(jobSkills);

        Set<String> jobTokens = extractTokens(
                String.join(" ", safeText(job.getTitle()), safeText(job.getDescription()), safeText(job.getSkills())));
        Set<String> titleTokens = extractTokens(safeText(job.getTitle()));

        double skillScore = jobSkills.isEmpty() ? 0.0 : (double) skillMatches.size() / jobSkills.size();
        double textScore = jaccardSimilarity(resumeTokens, jobTokens);
        double titleScore = titleTokens.isEmpty() ? 0.0
                : (double) countIntersection(resumeTokens, titleTokens) / titleTokens.size();

        double combinedScore = SKILL_WEIGHT * skillScore + TEXT_WEIGHT * textScore + TITLE_WEIGHT * titleScore;
        double normalizedScore = Math.round(combinedScore * 10000.0) / 100.0;

        if (normalizedScore <= 0) {
            return Optional.empty();
        }

        return Optional.of(JobMatchResponse.builder()
                .jobId(job.getId())
                .jobTitle(job.getTitle())
                .score(normalizedScore)
                .matchedSkills(String.join(", ", skillMatches))
                .build());
    }

    private Set<String> extractTokens(String text) {
        if (text == null || text.isBlank()) {
            return new HashSet<>();
        }

        return Arrays.stream(text.toLowerCase().split("[^a-z0-9]+"))
                .map(String::trim)
                .filter(token -> !token.isBlank())
                .filter(token -> token.length() > 1)
                .filter(token -> !STOP_WORDS.contains(token))
                .collect(Collectors.toSet());
    }

    private double jaccardSimilarity(Set<String> a, Set<String> b) {
        if (a.isEmpty() || b.isEmpty()) {
            return 0.0;
        }
        Set<String> intersection = new HashSet<>(a);
        intersection.retainAll(b);
        Set<String> union = new HashSet<>(a);
        union.addAll(b);
        return (double) intersection.size() / union.size();
    }

    private int countIntersection(Set<String> a, Set<String> b) {
        Set<String> intersection = new HashSet<>(a);
        intersection.retainAll(b);
        return intersection.size();
    }

    private Set<String> splitSkills(String skills) {
        if (skills == null || skills.isBlank())
            return new HashSet<>();
        return Arrays.stream(skills.toLowerCase().split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .collect(Collectors.toSet());
    }

    private String safeText(String text) {
        return text == null ? "" : text;
    }
}
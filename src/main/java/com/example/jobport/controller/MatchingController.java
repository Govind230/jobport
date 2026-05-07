package com.example.jobport.controller;

import com.example.jobport.dto.JobMatchResponse;
import com.example.jobport.service.MatchingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Page; 
import org.springframework.data.domain.Pageable;

@RestController
@RequestMapping("/matches")
@RequiredArgsConstructor
public class MatchingController {

    private final MatchingService matchingService;

    @GetMapping("/resume/{resumeId}")
    public ResponseEntity<Page<JobMatchResponse>> matchJobsForResume(
            @PathVariable Long resumeId,
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(matchingService.matchJobsForResume(resumeId, pageable));
    }
}
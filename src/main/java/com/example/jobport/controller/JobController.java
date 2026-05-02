package com.example.jobport.controller;

import com.example.jobport.dto.JobRequest;
import com.example.jobport.dto.JobResponse;
import com.example.jobport.service.JobService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;

    // Create a job (later will be auth‑protected)
    @PostMapping
    public ResponseEntity<JobResponse> createJob(
            @RequestBody @Valid JobRequest request,
            @RequestParam Long recruiterId
    ) {
        JobResponse response = jobService.createJob(request, recruiterId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Public list of all jobs
    @GetMapping
    public ResponseEntity<List<JobResponse>> getAllJobs() {

        return ResponseEntity.ok(jobService.getAllJobs());
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobResponse> getJobById(@PathVariable Long id) {
        return ResponseEntity.ok(jobService.getJobById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<JobResponse> updateJob(
            @PathVariable Long id,
            @RequestBody @Valid JobRequest request
    ) {
        return ResponseEntity.ok(jobService.updateJob(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteJob(@PathVariable Long id) {
        jobService.deleteJob(id);
        return ResponseEntity.noContent().build();
    }
}

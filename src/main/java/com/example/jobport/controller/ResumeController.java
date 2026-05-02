package com.example.jobport.controller;

import com.example.jobport.dto.ResumeResponse;
import com.example.jobport.service.ResumeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/resumes")
@RequiredArgsConstructor
public class ResumeController {

    private final ResumeService resumeService;

    @PostMapping("/upload")
    public ResponseEntity<ResumeResponse> uploadResume(
            @RequestParam Long userId,
            @RequestParam MultipartFile file
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(resumeService.uploadResume(userId, file));
    }

    @GetMapping
    public ResponseEntity<List<ResumeResponse>> getAllResumes() {
        return ResponseEntity.ok(resumeService.getAllResumes());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResumeResponse> getResumeById(@PathVariable Long id) {
        return ResponseEntity.ok(resumeService.getResumeById(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ResumeResponse>> getResumesByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(resumeService.getResumesByUserId(userId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteResume(@PathVariable Long id) {
        resumeService.deleteResume(id);
        return ResponseEntity.noContent().build();
    }
}
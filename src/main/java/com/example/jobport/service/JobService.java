package com.example.jobport.service;

import com.example.jobport.dto.JobRequest;
import com.example.jobport.dto.JobResponse;
import com.example.jobport.entity.Job;
import com.example.jobport.entity.User;
import com.example.jobport.dao.JobRepository;
import com.example.jobport.dao.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class JobService {

    private final JobRepository jobRepository;
    private final UserRepository userRepository;

    public JobResponse createJob(JobRequest request, Long recruiterId) {
        User recruiter = userRepository.findById(recruiterId)
                .orElseThrow(() -> new RuntimeException("Recruiter not found with id: " + recruiterId));

        if (recruiter.getRole() == null || !recruiter.getRole().name().equals("RECRUITER")) {
            throw new RuntimeException("User is not a recruiter");
        }

        Job job = Job.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .skills(request.getSkills())
                .location(request.getLocation())
                .salary(request.getSalary())
                .recruiter(recruiter)
                .build();

        Job saved = jobRepository.save(job);
        return mapToResponse(saved);
    }

    public List<JobResponse> getAllJobs() {
        return jobRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public JobResponse getJobById(Long id) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Job not found with id: " + id));
        return mapToResponse(job);
    }

    public JobResponse updateJob(Long id, JobRequest request) {
        Job existingJob = jobRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Job not found with id: " + id));

        existingJob.setTitle(request.getTitle());
        existingJob.setDescription(request.getDescription());
        existingJob.setSkills(request.getSkills());
        existingJob.setLocation(request.getLocation());
        existingJob.setSalary(request.getSalary());

        Job updated = jobRepository.save(existingJob);
        return mapToResponse(updated);
    }

    public void deleteJob(Long id) {
        if (!jobRepository.existsById(id)) {
            throw new RuntimeException("Job not found with id: " + id);
        }
        jobRepository.deleteById(id);
    }

    private JobResponse mapToResponse(Job job) {
        return JobResponse.builder()
                .id(job.getId())
                .title(job.getTitle())
                .description(job.getDescription())
                .skills(job.getSkills())
                .location(job.getLocation())
                .salary(job.getSalary())
                .recruiterId(job.getRecruiter() != null ? job.getRecruiter().getId() : null)
                .build();
    }
}
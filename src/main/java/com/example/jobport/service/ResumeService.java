package com.example.jobport.service;

import com.example.jobport.dto.ResumeResponse;
import com.example.jobport.entity.Resume;
import com.example.jobport.dao.ResumeRepository;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.apache.pdfbox.Loader;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ResumeService {

    private final ResumeRepository resumeRepository;

    private static final String UPLOAD_DIR = System.getProperty("user.dir") + File.separator + "uploads" + File.separator + "resumes";

    public ResumeResponse uploadResume(Long userId, MultipartFile file) {
        try {
            File dir = new File(UPLOAD_DIR);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String originalName = file.getOriginalFilename();
            String safeName = UUID.randomUUID() + "_" + originalName.replaceAll("\\s+", "_");
            File savedFile = new File(dir, safeName);

            file.transferTo(savedFile);

            String extractedText = extractPdfText(savedFile);
            String extractedSkills = extractSkills(extractedText);

            Resume resume = Resume.builder()
                    .userId(userId)
                    .originalFileName(originalName)
                    .filePath(savedFile.getAbsolutePath())
                    .extractedText(extractedText)
                    .extractedSkills(extractedSkills)
                    .uploadedAt(LocalDateTime.now())
                    .build();

            Resume saved = resumeRepository.save(resume);
            return mapToResponse(saved);

        } catch (Exception e) {
            throw new RuntimeException("Failed to upload resume: " + e.getMessage(), e);
        }
    }

    public String extractPdfText(File file) {
        try (PDDocument document = Loader.loadPDF(file)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        } catch (IOException e) {
            throw new RuntimeException("Failed to extract PDF text");
        }
    }

    private String extractSkills(String text) {
        if (text == null) return "";
        String lower = text.toLowerCase();

        StringBuilder skills = new StringBuilder();
        String[] knownSkills = {
                "java", "spring", "spring boot", "mysql", "postgresql", "redis",
                "kafka", "docker", "kubernetes", "microservices", "hibernate", "jpa"
        };

        for (String skill : knownSkills) {
            if (lower.contains(skill)) {
                if (!skills.isEmpty()) skills.append(", ");
                skills.append(skill);
            }
        }
        return skills.toString();
    }

    public List<ResumeResponse> getAllResumes() {
        return resumeRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public ResumeResponse getResumeById(Long id) {
        Resume resume = resumeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Resume not found with id: " + id));
        return mapToResponse(resume);
    }

    public List<ResumeResponse> getResumesByUserId(Long userId) {
        return resumeRepository.findByUserId(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public void deleteResume(Long id) {
        if (!resumeRepository.existsById(id)) {
            throw new RuntimeException("Resume not found with id: " + id);
        }
        resumeRepository.deleteById(id);
    }

    private ResumeResponse mapToResponse(Resume resume) {
        return ResumeResponse.builder()
                .id(resume.getId())
                .userId(resume.getUserId())
                .originalFileName(resume.getOriginalFileName())
                .filePath(resume.getFilePath())
                .extractedText(resume.getExtractedText())
                .extractedSkills(resume.getExtractedSkills())
                .uploadedAt(resume.getUploadedAt())
                .build();
    }
}
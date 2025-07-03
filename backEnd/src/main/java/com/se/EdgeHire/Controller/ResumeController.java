package com.se.EdgeHire.Controller;

import com.se.EdgeHire.Entity.Resume;
import com.se.EdgeHire.Service.ResumeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/resume")
@Slf4j
public class ResumeController {
    @Autowired
    private ResumeService resumeService;

    @GetMapping
    public List<Resume> getAllResumes() {
        return resumeService.getAllResumes();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Resume> getResumeById(@PathVariable Integer id) {
        Optional<Resume> resume = resumeService.getResumeById(id);
        return resume.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    public List<Resume> getResumesByUserId(@PathVariable Integer userId) {
        return resumeService.getResumesByUserId(userId);
    }

    @PostMapping
    public Resume createResume(@RequestBody Resume resume) {
        log.info(resume.getAvatar());
        return resumeService.saveResume(resume);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Resume> updateResume(@PathVariable Integer id, @RequestBody Resume resume) {
        if (!resumeService.getResumeById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        resume.setId(id);
        return ResponseEntity.ok(resumeService.saveResume(resume));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteResume(@PathVariable Integer id) {
        if (!resumeService.getResumeById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        resumeService.deleteResumeAndPosts(id);
        return ResponseEntity.noContent().build();
    }
}


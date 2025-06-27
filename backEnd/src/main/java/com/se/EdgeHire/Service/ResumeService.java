package com.se.EdgeHire.Service;

import com.se.EdgeHire.Entity.Resume;
import com.se.EdgeHire.Repository.ResumeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ResumeService {
    @Autowired
    private ResumeRepository resumeRepository;

    public List<Resume> getAllResumes() {
        return resumeRepository.findAll();
    }

    public Optional<Resume> getResumeById(Integer id) {
        return resumeRepository.findById(id);
    }

    public List<Resume> getResumesByUserId(Integer userId) {
        return resumeRepository.findByUserId(userId);
    }

    public Resume saveResume(Resume resume) {
        if (resume.getCreateTime() == null) {
            resume.setCreateTime(java.time.LocalDateTime.now());
        }
        return resumeRepository.save(resume);
    }

    public void deleteResume(Integer id) {
        resumeRepository.deleteById(id);
    }
}


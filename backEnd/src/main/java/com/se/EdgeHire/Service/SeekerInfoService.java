package com.se.EdgeHire.Service;

import com.se.EdgeHire.Entity.SeekerInfo;
import com.se.EdgeHire.Repository.SeekerInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SeekerInfoService {
    @Autowired
    private SeekerInfoRepository seekerInfoRepository;

    public List<SeekerInfo> findAll() {
        return seekerInfoRepository.findAll();
    }

    public Optional<SeekerInfo> findById(Integer id) {
        return seekerInfoRepository.findById(id);
    }

    public Optional<SeekerInfo> findByUserId(Integer userId) {
        return seekerInfoRepository.findByUserId(userId);
    }

    public SeekerInfo save(SeekerInfo seekerInfo) {
        return seekerInfoRepository.save(seekerInfo);
    }

    public void deleteById(Integer id) {
        seekerInfoRepository.deleteById(id);
    }
}

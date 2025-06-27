package com.se.EdgeHire.Service;

import com.se.EdgeHire.Entity.Info;
import com.se.EdgeHire.Repository.InfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class InfoService {
    @Autowired
    private InfoRepository infoRepository;

    public List<Info> findByUserId(Integer userId) {
        return infoRepository.findByUserId(userId);
    }

    public Info createInfo(Info info) {
        List<Info> existList = infoRepository.findByUserId(info.getUserId());
        if (existList != null && !existList.isEmpty()) {
            return existList.get(0);
        }
        return infoRepository.save(info);
    }

    public Info updateInfo(Integer id, Info info) {
        if (!infoRepository.existsById(id)) {
            return null;
        }
        info.setId(id);
        return infoRepository.save(info);
    }
}

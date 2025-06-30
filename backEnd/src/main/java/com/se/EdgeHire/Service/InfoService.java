package com.se.EdgeHire.Service;

import com.se.EdgeHire.Entity.Info;
import com.se.EdgeHire.Entity.User;
import com.se.EdgeHire.Repository.InfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Optional;

@Service
public class InfoService {
    @Autowired
    private InfoRepository infoRepository;

    public List<Info> getAllInfos() {
        return infoRepository.findAll();
    }

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

    public String getAvatarByUserId(Integer userId) {
        List<Info> infos = infoRepository.findByUserId(userId);
        if (infos != null && !infos.isEmpty()) {
            return infos.get(0).getAvatar();
        }
        return null;
    }
}

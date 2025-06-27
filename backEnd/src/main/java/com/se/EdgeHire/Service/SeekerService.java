package com.se.EdgeHire.Service;

import com.se.EdgeHire.Entity.SeekerInfo;
import com.se.EdgeHire.Entity.User;
import com.se.EdgeHire.Repository.SeekerInfoRepository;
import com.se.EdgeHire.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SeekerService {
    
    @Autowired
    private SeekerInfoRepository seekerInfoRepository;
    
    @Autowired
    private UserRepository userRepository;

    /**
     * 根据用户ID获取求职者信息
     */
    public Optional<SeekerInfo> getSeekerInfoByUserId(Integer userId) {
        return seekerInfoRepository.findByUserId(userId);
    }

    /**
     * 创建求职者信息
     */
    public SeekerInfo createSeekerInfo(SeekerInfo seekerInfo) {
        // 验证用户是否存在且角色为求职者
        Optional<User> user = userRepository.findById(seekerInfo.getUserId());
        if (user.isEmpty() || user.get().getRole() != 1) {
            throw new IllegalArgumentException("用户不存在或不是求职者角色");
        }
        
        // 检查是否已存在求职者信息
        if (seekerInfoRepository.existsByUserId(seekerInfo.getUserId())) {
            throw new IllegalArgumentException("该用户的求职者信息已存在");
        }
        
        return seekerInfoRepository.save(seekerInfo);
    }

    /**
     * 更新求职者信息
     */
    public SeekerInfo updateSeekerInfo(Integer id, SeekerInfo seekerInfo) {
        if (!seekerInfoRepository.existsById(id)) {
            throw new IllegalArgumentException("求职者信息不存在");
        }
        seekerInfo.setId(id);
        return seekerInfoRepository.save(seekerInfo);
    }

    /**
     * 根据用户ID更新求职者信息
     */
    public SeekerInfo updateSeekerInfoByUserId(Integer userId, SeekerInfo seekerInfo) {
        Optional<SeekerInfo> existingSeekerInfo = seekerInfoRepository.findByUserId(userId);
        if (existingSeekerInfo.isEmpty()) {
            throw new IllegalArgumentException("求职者信息不存在");
        }
        
        SeekerInfo toUpdate = existingSeekerInfo.get();
        toUpdate.setEducation(seekerInfo.getEducation());
        toUpdate.setSchool(seekerInfo.getSchool());
        toUpdate.setFavor(seekerInfo.getFavor());
        toUpdate.setMembership(seekerInfo.getMembership());
        
        return seekerInfoRepository.save(toUpdate);
    }

    /**
     * 根据条件搜索求职者
     */
    public List<SeekerInfo> searchSeekers(Integer minEducation, String favor) {
        if (minEducation != null && favor != null && !favor.trim().isEmpty()) {
            return seekerInfoRepository.findByEducationAndFavor(minEducation, favor.trim());
        } else if (minEducation != null) {
            return seekerInfoRepository.findByEducation(minEducation);
        } else if (favor != null && !favor.trim().isEmpty()) {
            return seekerInfoRepository.findByFavorContaining(favor.trim());
        } else {
            return seekerInfoRepository.findAll();
        }
    }

    /**
     * 根据会员等级获取求职者
     */
    public List<SeekerInfo> getSeekersByMembership(Integer membership) {
        return seekerInfoRepository.findByMembership(membership);
    }

    /**
     * 获取所有求职者信息
     */
    public List<SeekerInfo> getAllSeekers() {
        return seekerInfoRepository.findAll();
    }

    /**
     * 验证用户是否为求职者
     */
    public boolean isSeeker(Integer userId) {
        Optional<User> user = userRepository.findById(userId);
        return user.isPresent() && user.get().getRole() == 1;
    }
}

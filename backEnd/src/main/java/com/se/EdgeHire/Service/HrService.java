package com.se.EdgeHire.Service;

import com.se.EdgeHire.Entity.HrInfo;
import com.se.EdgeHire.Entity.User;
import com.se.EdgeHire.Repository.HrInfoRepository;
import com.se.EdgeHire.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class HrService {
    
    @Autowired
    private HrInfoRepository hrInfoRepository;
    
    @Autowired
    private UserRepository userRepository;

    /**
     * 根据用户ID获取HR信息
     */
    public Optional<HrInfo> getHrInfoByUserId(Integer userId) {
        return hrInfoRepository.findByUserId(userId);
    }

    /**
     * 创建HR信息
     */
    public HrInfo createHrInfo(HrInfo hrInfo) {
        // 验证用户是否存在且角色为HR
        Optional<User> user = userRepository.findById(hrInfo.getUserId());
        if (user.isEmpty() || user.get().getRole() != 2) {
            throw new IllegalArgumentException("用户不存在或不是HR角色");
        }
        
        // 检查是否已存在HR信息
        if (hrInfoRepository.existsByUserId(hrInfo.getUserId())) {
            throw new IllegalArgumentException("该用户的HR信息已存在");
        }
        
        return hrInfoRepository.save(hrInfo);
    }

    /**
     * 更新HR信息
     */
    public HrInfo updateHrInfo(Integer id, HrInfo hrInfo) {
        if (!hrInfoRepository.existsById(id)) {
            throw new IllegalArgumentException("HR信息不存在");
        }
        hrInfo.setId(id);
        return hrInfoRepository.save(hrInfo);
    }

    /**
     * 根据用户ID更新HR信息
     */
    public HrInfo updateHrInfoByUserId(Integer userId, HrInfo hrInfo) {
        Optional<HrInfo> existingHrInfo = hrInfoRepository.findByUserId(userId);
        if (existingHrInfo.isEmpty()) {
            throw new IllegalArgumentException("HR信息不存在");
        }
        
        HrInfo toUpdate = existingHrInfo.get();
        toUpdate.setCompany(hrInfo.getCompany());
        toUpdate.setPosition(hrInfo.getPosition());
        toUpdate.setExperience(hrInfo.getExperience());
        
        return hrInfoRepository.save(toUpdate);
    }

    /**
     * 删除HR信息
     */
    public void deleteHrInfo(Integer id) {
        hrInfoRepository.deleteById(id);
    }

    /**
     * 获取所有HR信息
     */
    public List<HrInfo> getAllHrInfo() {
        return hrInfoRepository.findAll();
    }

    /**
     * 验证用户是否为HR
     */
    public boolean isHr(Integer userId) {
        Optional<User> user = userRepository.findById(userId);
        return user.isPresent() && user.get().getRole() == 2;
    }
}

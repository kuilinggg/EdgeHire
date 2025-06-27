package com.se.EdgeHire;

import com.se.EdgeHire.Entity.HrInfo;
import com.se.EdgeHire.Entity.SeekerInfo;
import com.se.EdgeHire.Entity.User;
import com.se.EdgeHire.Service.HrService;
import com.se.EdgeHire.Service.SeekerService;
import com.se.EdgeHire.Repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class HrModuleTest {

    @Autowired
    private HrService hrService;

    @Autowired
    private SeekerService seekerService;

    @Autowired
    private UserRepository userRepository;

    @Test
    public void testHrServiceBasicFunctionality() {
        // 测试HR服务的基本功能
        
        // 创建测试用户
        User testUser = new User();
        testUser.setUsername("test_hr_" + System.currentTimeMillis());
        testUser.setPassword("test_password");
        testUser.setRole(2); // HR角色
        
        User savedUser = userRepository.save(testUser);
        
        // 创建HR信息
        HrInfo hrInfo = new HrInfo();
        hrInfo.setUserId(savedUser.getId());
        hrInfo.setCompany("测试公司");
        hrInfo.setPosition("Java开发工程师");
        hrInfo.setExperience("3年招聘经验");
        
        HrInfo savedHrInfo = hrService.createHrInfo(hrInfo);
        
        assertNotNull(savedHrInfo);
        assertEquals("测试公司", savedHrInfo.getCompany());
        assertEquals("Java开发工程师", savedHrInfo.getPosition());
        
        // 验证HR身份
        assertTrue(hrService.isHr(savedUser.getId()));
        
        // 清理测试数据
        hrService.deleteHrInfo(savedHrInfo.getId());
        userRepository.delete(savedUser);
    }

    @Test
    public void testSeekerServiceBasicFunctionality() {
        // 测试求职者服务的基本功能
        
        // 创建测试用户
        User testUser = new User();
        testUser.setUsername("test_seeker_" + System.currentTimeMillis());
        testUser.setPassword("test_password");
        testUser.setRole(1); // 求职者角色
        
        User savedUser = userRepository.save(testUser);
        
        // 创建求职者信息
        SeekerInfo seekerInfo = new SeekerInfo();
        seekerInfo.setUserId(savedUser.getId());
        seekerInfo.setEducation(5); // 本科
        seekerInfo.setSchool("测试大学");
        seekerInfo.setFavor("Java开发");
        seekerInfo.setMembership(0); // 普通会员
        
        SeekerInfo savedSeekerInfo = seekerService.createSeekerInfo(seekerInfo);
        
        assertNotNull(savedSeekerInfo);
        assertEquals(5, savedSeekerInfo.getEducation());
        assertEquals("测试大学", savedSeekerInfo.getSchool());
        assertEquals("Java开发", savedSeekerInfo.getFavor());
        
        // 验证求职者身份
        assertTrue(seekerService.isSeeker(savedUser.getId()));
        
        // 清理测试数据
        userRepository.delete(savedUser);
    }
}

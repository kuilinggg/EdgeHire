package com.se.EdgeHire.Controller;

import com.se.EdgeHire.Entity.SeekerInfo;
import com.se.EdgeHire.Service.SeekerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/seeker")
@CrossOrigin(origins = "*")
public class SeekerController {
    
    @Autowired
    private SeekerService seekerService;

    /**
     * 根据用户ID获取求职者信息
     */
    @GetMapping("/info/{userId}")
    public ResponseEntity<SeekerInfo> getSeekerInfoByUserId(@PathVariable Integer userId) {
        Optional<SeekerInfo> seekerInfo = seekerService.getSeekerInfoByUserId(userId);
        return seekerInfo.map(ResponseEntity::ok)
                        .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 创建求职者信息
     */
    @PostMapping("/info")
    public ResponseEntity<?> createSeekerInfo(@RequestBody SeekerInfo seekerInfo) {
        try {
            SeekerInfo created = seekerService.createSeekerInfo(seekerInfo);
            return ResponseEntity.ok(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 更新求职者信息
     */
    @PutMapping("/info/{id}")
    public ResponseEntity<?> updateSeekerInfo(@PathVariable Integer id, @RequestBody SeekerInfo seekerInfo) {
        try {
            SeekerInfo updated = seekerService.updateSeekerInfo(id, seekerInfo);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 根据用户ID更新求职者信息
     */
    @PutMapping("/info/user/{userId}")
    public ResponseEntity<?> updateSeekerInfoByUserId(@PathVariable Integer userId, @RequestBody SeekerInfo seekerInfo) {
        try {
            SeekerInfo updated = seekerService.updateSeekerInfoByUserId(userId, seekerInfo);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 搜索求职者
     */
    @GetMapping("/search")
    public ResponseEntity<List<SeekerInfo>> searchSeekers(
            @RequestParam(required = false) Integer minEducation,
            @RequestParam(required = false) String favor) {
        List<SeekerInfo> seekers = seekerService.searchSeekers(minEducation, favor);
        return ResponseEntity.ok(seekers);
    }

    /**
     * 根据会员等级获取求职者
     */
    @GetMapping("/membership/{membership}")
    public ResponseEntity<List<SeekerInfo>> getSeekersByMembership(@PathVariable Integer membership) {
        List<SeekerInfo> seekers = seekerService.getSeekersByMembership(membership);
        return ResponseEntity.ok(seekers);
    }

    /**
     * 获取所有求职者
     */
    @GetMapping("/all")
    public ResponseEntity<List<SeekerInfo>> getAllSeekers() {
        List<SeekerInfo> seekers = seekerService.getAllSeekers();
        return ResponseEntity.ok(seekers);
    }

    /**
     * 验证用户是否为求职者
     */
    @GetMapping("/verify/{userId}")
    public ResponseEntity<Map<String, Boolean>> verifySeeker(@PathVariable Integer userId) {
        boolean isSeeker = seekerService.isSeeker(userId);
        return ResponseEntity.ok(Map.of("isSeeker", isSeeker));
    }
}

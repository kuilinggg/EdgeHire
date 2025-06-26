package com.se.EdgeHire.Controller;

import com.se.EdgeHire.Entity.Info;
import com.se.EdgeHire.Repository.InfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/t_info")
public class InfoController {
    @Autowired
    private InfoRepository infoRepository;

    @GetMapping("/user/{userId}")
    public ResponseEntity<Info> getInfoByUserId(@PathVariable Integer userId) {
        List<Info> infos = infoRepository.findByUserId(userId);
        if (infos != null && !infos.isEmpty()) {
            return ResponseEntity.ok(infos.get(0));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<Info> createInfo(@RequestBody Info info) {
        // 检查userId唯一性
        List<Info> existList = infoRepository.findByUserId(info.getUserId());
        if (existList != null && !existList.isEmpty()) {
            // 已存在，返回已存在的记录（或可返回400/409错误）
            return ResponseEntity.ok(existList.get(0));
        }
        Info saved = infoRepository.save(info);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Info> updateInfo(@PathVariable Integer id, @RequestBody Info info) {
        if (!infoRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        info.setId(id);
        Info updated = infoRepository.save(info);
        return ResponseEntity.ok(updated);
    }
}

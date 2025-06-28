package com.se.EdgeHire.Controller;

import com.se.EdgeHire.Entity.Info;
import com.se.EdgeHire.Service.InfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/t_info")
public class InfoController {
    @Autowired
    private InfoService infoService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<Info> getInfoByUserId(@PathVariable Integer userId) {
        List<Info> infos = infoService.findByUserId(userId);
        if (infos != null && !infos.isEmpty()) {
            return ResponseEntity.ok(infos.get(0));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<Info> createInfo(@RequestBody Info info) {
        Info saved = infoService.createInfo(info);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Info> updateInfo(@PathVariable Integer id, @RequestBody Info info) {
        Info updated = infoService.updateInfo(id, info);
        if (updated == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/avatar/{userId}")
    public ResponseEntity<String> getAvatar(@PathVariable Integer userId){
        String avatar = infoService.getAvatarByUserId(userId);
        if (avatar != null && !avatar.isEmpty()) {
            return ResponseEntity.ok(avatar);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}

package com.se.EdgeHire.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/upload")
public class UploadController {
    @PostMapping
    public ResponseEntity<Map<String, String>> upload(@RequestParam("file") MultipartFile file) throws IOException {
        String uploadDir = "uploads/";
        File dir = new File(uploadDir);
        if (!dir.exists()) dir.mkdirs();
        String filename = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        File dest = new File(uploadDir + filename);
        file.transferTo(dest);
        String url = "/uploads/" + filename;
        Map<String, String> result = new HashMap<>();
        result.put("url", url);
        return ResponseEntity.ok(result);
    }

}

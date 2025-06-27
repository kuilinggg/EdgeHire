package com.se.EdgeHire.Controller;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;

@RestController
@RequestMapping("/api/files")
@CrossOrigin(origins = "http://localhost:5173")
public class UploadController {

    private static final String UPLOAD_DIR = "uploads/";

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("文件不能为空");
        }

        try {
            // 生成唯一文件名
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            File dest = new File(UPLOAD_DIR + fileName);
            // 确保目录存在
            dest.getParentFile().mkdirs();
            // 保存文件
            file.transferTo(dest.getAbsoluteFile());
            // 返回文件访问路径
            String fileUrl = "http://localhost:8080/api/files/" + fileName;
            return ResponseEntity.ok(fileUrl);
        } catch (IOException e) {
            System.out.println(e.toString());
            return ResponseEntity.status(500).body("文件上传失败");
        }
    }

    @GetMapping("/{fileName}")
    public ResponseEntity<Resource> getFile(@PathVariable String fileName) throws IOException {
        File file = new File(UPLOAD_DIR + fileName);
        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new FileSystemResource(file);

        // 动态获取 MIME 类型
        String mimeType = Files.probeContentType(file.toPath());
        if (mimeType == null) {
            mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE; // 默认类型
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getName() + "\"")
                .contentType(MediaType.parseMediaType(mimeType))
                .body(resource);
    }
}
package com.se.EdgeHire.Controller;

import com.se.EdgeHire.Entity.HrInfo;
import com.se.EdgeHire.Service.HrService;
import com.se.EdgeHire.Service.HrStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/hr")
@CrossOrigin(origins = "*")
public class HrController {
    
    @Autowired
    private HrService hrService;
    
    @Autowired
    private HrStatisticsService hrStatisticsService;

    /**
     * 根据用户ID获取HR信息
     */
    @GetMapping("/info/{userId}")
    public ResponseEntity<HrInfo> getHrInfoByUserId(@PathVariable Integer userId) {
        Optional<HrInfo> hrInfo = hrService.getHrInfoByUserId(userId);
        return hrInfo.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 创建HR信息
     */
    @PostMapping("/info")
    public ResponseEntity<?> createHrInfo(@RequestBody HrInfo hrInfo) {
        try {
            HrInfo created = hrService.createHrInfo(hrInfo);
            return ResponseEntity.ok(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 更新HR信息
     */
    @PutMapping("/info/{id}")
    public ResponseEntity<?> updateHrInfo(@PathVariable Integer id, @RequestBody HrInfo hrInfo) {
        try {
            HrInfo updated = hrService.updateHrInfo(id, hrInfo);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 根据用户ID更新HR信息
     */
    @PutMapping("/info/user/{userId}")
    public ResponseEntity<?> updateHrInfoByUserId(@PathVariable Integer userId, @RequestBody HrInfo hrInfo) {
        try {
            HrInfo updated = hrService.updateHrInfoByUserId(userId, hrInfo);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 获取HR主页KPI数据
     */
    @GetMapping("/kpi/{hrId}")
    public ResponseEntity<Map<String, Object>> getHrKpiData(@PathVariable Integer hrId) {
        try {
            Map<String, Object> kpiData = hrStatisticsService.getHrKpiData(hrId);
            return ResponseEntity.ok(kpiData);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 获取HR详细统计数据
     */
    @GetMapping("/stats/{hrId}")
    public ResponseEntity<Map<String, Object>> getHrDetailedStats(@PathVariable Integer hrId) {
        try {
            Map<String, Object> stats = hrStatisticsService.getHrDetailedStats(hrId);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 验证用户是否为HR
     */
    @GetMapping("/verify/{userId}")
    public ResponseEntity<Map<String, Boolean>> verifyHr(@PathVariable Integer userId) {
        boolean isHr = hrService.isHr(userId);
        return ResponseEntity.ok(Map.of("isHr", isHr));
    }
}

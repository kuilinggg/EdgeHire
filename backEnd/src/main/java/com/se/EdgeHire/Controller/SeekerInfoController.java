package com.se.EdgeHire.Controller;

import com.se.EdgeHire.Entity.SeekerInfo;
import com.se.EdgeHire.Service.SeekerInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/seeker-info")
public class SeekerInfoController {
    @Autowired
    private SeekerInfoService seekerInfoService;

    @GetMapping
    public List<SeekerInfo> getAll() {
        return seekerInfoService.findAll();
    }

    @GetMapping("/{id}")
    public Optional<SeekerInfo> getById(@PathVariable Integer id) {
        return seekerInfoService.findById(id);
    }

    @PostMapping
    public SeekerInfo create(@RequestBody SeekerInfo seekerInfo) {
        return seekerInfoService.save(seekerInfo);
    }

    @PutMapping("/{id}")
    public SeekerInfo update(@PathVariable Integer id, @RequestBody SeekerInfo seekerInfo) {
        seekerInfo.setId(id);
        return seekerInfoService.save(seekerInfo);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        seekerInfoService.deleteById(id);
    }

    @GetMapping("/user/{userId}")
    public Optional<SeekerInfo> getByUserId(@PathVariable Integer userId) {
        return seekerInfoService.findByUserId(userId);
    }
}

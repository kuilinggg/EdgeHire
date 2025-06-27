package com.se.EdgeHire.Entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "t_hr_info")
@Data
public class HrInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "company", length = 255)
    private String company;

    @Column(name = "position", length = 255)
    private String position;

    @Column(name = "experience", length = 255)
    private String experience;
}

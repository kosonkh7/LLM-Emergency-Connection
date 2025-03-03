package com.aivle.mini7.model;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "navigation")
public class Navigation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private double distance;
    private int duration;
    private String instructions;
    private int pointIndex;
    private int type;

    @ManyToOne
    @JoinColumn(name = "emergency_recommendation_id")  // 외래 키 이름
    private EmergencyRecommendation emergencyRecommendation;

}
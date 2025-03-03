package com.aivle.mini7.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "emergency_recommendation")
public class EmergencyRecommendation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private double distance;
    private int time;

    @ManyToOne
    @JoinColumn(name = "emergency_id")
    private Emergency emergency;

    @ManyToOne
    @JoinColumn(name = "emergency_treatment_id")
    private EmergencyTreatment emergencyTreatment;

    @OneToMany(mappedBy = "emergencyRecommendation", cascade = CascadeType.ALL)
    private List<Navigation> navigationInstructions;


}


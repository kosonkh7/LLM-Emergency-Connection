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
@Table(name = "emergency_treatment")
public class EmergencyTreatment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private int status;
    private int scale;
    private String text;
    private String message;
    private String keyword;

    @OneToMany(mappedBy = "emergencyTreatment", cascade = CascadeType.ALL)
    private List<EmergencyRecommendation> recommendations;

}

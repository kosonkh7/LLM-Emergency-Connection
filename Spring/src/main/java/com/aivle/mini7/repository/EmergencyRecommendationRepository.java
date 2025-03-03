package com.aivle.mini7.repository;

import com.aivle.mini7.model.EmergencyRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmergencyRecommendationRepository extends JpaRepository<EmergencyRecommendation, Long>{

}

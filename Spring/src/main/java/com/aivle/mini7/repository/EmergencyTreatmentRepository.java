package com.aivle.mini7.repository;

import com.aivle.mini7.model.EmergencyTreatment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmergencyTreatmentRepository extends JpaRepository<EmergencyTreatment, Long>{

}

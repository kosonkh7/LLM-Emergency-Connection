package com.aivle.mini7.service;

import com.aivle.mini7.client.dto.HospitalResponse;
import com.aivle.mini7.model.*;
import com.aivle.mini7.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmergencyService {

    @Autowired
    private EmergencyRepository emergencyRepository;

    @Autowired
    private EmergencyTreatmentRepository emergencyTreatmentRepository;

    @Autowired
    private EmergencyRecommendationRepository emergencyRecommendationRepository;

    @Autowired
    private NavigationRepository navigationRepository;

    @Autowired
    private LocationPointRepository locationPointRepository;

    @Transactional
    public void saveEmergencyData(HospitalResponse response) {
        EmergencyTreatment treatment = new EmergencyTreatment();
        treatment.setStatus((int) response.getStatus());
        treatment.setScale(response.getScale());
        treatment.setText(response.getText());
        treatment.setMessage(response.getMessage());
        treatment.setKeyword(response.getKeyword());

        // 병원 추천 데이터 저장
        response.getRecommend().forEach(recommendation -> {
            Long hospitalId = recommendation.getHospitalId();

            if (hospitalId == null) {
                throw new IllegalArgumentException("Hospital ID must not be null in the recommendation data.");
            }

            Emergency emergency = emergencyRepository.findById(hospitalId)
                    .orElseThrow(() -> new IllegalArgumentException("Hospital with ID " + hospitalId + " not found"));

            EmergencyRecommendation emergencyRecommendation = new EmergencyRecommendation();
            emergencyRecommendation.setDistance(recommendation.getDistance());
            emergencyRecommendation.setTime((int) recommendation.getTime());
            emergencyRecommendation.setEmergency(emergency);
            emergencyRecommendation.setEmergencyTreatment(treatment);

            // 내비게이션 데이터 저장
            recommendation.getNavigation().forEach(nav -> {
                Navigation navigation = new Navigation();
                navigation.setDistance(nav.getDistance());
                navigation.setDuration((int) nav.getDuration());
                navigation.setInstructions(nav.getInstructions());
                navigation.setPointIndex((int) nav.getPointIndex());
                navigation.setType((int) nav.getType());
                navigation.setEmergencyRecommendation(emergencyRecommendation);

                navigationRepository.save(navigation);
            });

            // 위치 데이터 저장
            recommendation.getPoints().forEach(point -> {
                LocationPoint location = new LocationPoint();
                location.setLatitude(point.get(1));
                location.setLongitude(point.get(0));
                location.setEmergencyRecommendation(emergencyRecommendation);

                locationPointRepository.save(location);
            });

            emergencyRecommendationRepository.save(emergencyRecommendation);
        });

        // 최상위 데이터 저장
        emergencyTreatmentRepository.save(treatment);
    }
}

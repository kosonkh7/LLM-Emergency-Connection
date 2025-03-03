package com.aivle.mini7.client.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class Hospital {
    private Long hospitalId;
    private String hospitalName;
    private String address;
    private String emergencyMedicalInstitutionType;
    private String phoneNumber1;
    private String phoneNumber3;
    private double latitude;
    private double longitude;
    private String region;
    private double distance;
    private double time;
    private List<List<Double>> points;
    private List<List<Double>> mapRegion;
    private List<Navigation> navigation;
    private int responseCode;
    private String responseMessage;
}

package com.aivle.mini7.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class HospitalResponse {
    private double status;
    private int scale;
    private String text;
    private String message;
    private String keyword;
    private List<Hospital> recommend;
}




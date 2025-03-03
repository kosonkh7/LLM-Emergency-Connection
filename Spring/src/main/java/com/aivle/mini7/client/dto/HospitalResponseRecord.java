package com.aivle.mini7.client.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class HospitalResponseRecord {
    private double status;
    private int scale;
    private String message;
    private String text;
    private String keyword;
    private List<Hospital> recommend;
}


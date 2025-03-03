package com.aivle.mini7.client.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Navigation {
    private double distance;
    private double duration;
    private String instructions;
    private double pointIndex;
    private double type;
}

package com.aivle.mini7.client.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PostData{
    private String text;
    private double latitude;
    private double longitude;
    private int optionNum;
}

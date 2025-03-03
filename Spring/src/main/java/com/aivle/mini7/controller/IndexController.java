package com.aivle.mini7.controller;

import com.aivle.mini7.client.api.FastApiClient;
import com.aivle.mini7.client.dto.HospitalResponse;
import com.aivle.mini7.client.dto.HospitalResponseRecord;
import com.aivle.mini7.client.dto.PostData;
import com.aivle.mini7.service.EmergencyService;
import com.aivle.mini7.service.LogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequiredArgsConstructor
@Slf4j
public class IndexController {

    private final FastApiClient fastApiClient;
    private final LogService logService;
    private final EmergencyService emergencyService;

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/recommend_hospital")
    public ModelAndView recommend_hospital(@RequestParam("text") String text, @RequestParam("latitude") double latitude, @RequestParam("longitude") double longitude, @RequestParam("optionNum") int optionNum) {
//    public ModelAndView recommend_hospital(@RequestParam("request") String request, @RequestParam("latitude") double latitude, @RequestParam("longitude") double longitude) {
        ModelAndView mv = new ModelAndView();
//        FastApiClient 를 호출한다.
        try {
//        List<HospitalResponse> hospitalList = fastApiClient.getHospital(request, latitude, longitude);
            HospitalResponse hospitalList = fastApiClient.getHospital(text, latitude, longitude, optionNum);
            log.info("hospital: {}", hospitalList);

//        emclass는 AI의 api를 고치기 힘들어서 일단 하드코딩으로 마무리한다.
            if (hospitalList != null) {
                logService.saveLog(hospitalList, text, latitude, longitude, 4);
                emergencyService.saveEmergencyData(hospitalList);
                mv.setViewName("recommend_hospital");
                mv.addObject("hospitalList", hospitalList);
            } else {
                log.warn("응급상황아님: 병원 추천 결과가 비어 있습니다.");
                mv.addObject("message", "응급상황아님");
            }
        } catch (Exception e) {
            log.warn("통신에러.");
            mv.addObject("message", "통신에러");
        }
        return mv;
    }

//    @PostMapping(value = "/recommend_hospital", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @PostMapping(value = "/recommend_hospital")
    public ModelAndView recommend_hospital(@RequestBody PostData postData) {
        String text = postData.getText();
        double latitude = postData.getLatitude();
        double longitude = postData.getLongitude();
        int optionNum = postData.getOptionNum();

        HospitalResponse hospitalList = fastApiClient.getHospital(text, latitude, longitude, optionNum);
        log.info("hospital: {}", hospitalList);

//        emclass는 AI의 api를 고치기 힘들어서 일단 하드코딩으로 마무리한다.
        if(hospitalList !=null){
            logService.saveLog(hospitalList, text, latitude, longitude,4);
        }

        ModelAndView mv = new ModelAndView();
        mv.setViewName("recommend_hospital");
        mv.addObject("hospitalList", hospitalList);

        return mv;
    }

    @PostMapping("/recommend_hospital_with_record")
    public ModelAndView recommend_hospital_with_record(
            @RequestParam("file") MultipartFile file,
            @RequestParam("latitude") double latitude,
            @RequestParam("longitude") double longitude
            ){
        HospitalResponseRecord hospitalList = fastApiClient.getHospitalWithRecord(file, latitude, longitude);

        log.info("hospital: {}", hospitalList);
        ModelAndView mv = new ModelAndView();
        mv.setViewName("recommend_hospital");

//        emclass는 AI의 api를 고치기 힘들어서 일단 하드코딩으로 마무리한다.
        if(hospitalList !=null){
            logService.saveLogRecord(hospitalList, hospitalList.getText(), latitude, longitude,4);
            mv.addObject("hospitalList", hospitalList);
        } else {
            log.warn("응급상황아님: 병원 추천 결과가 비어 있습니다.");
            mv.addObject("message", "응급상황아님");
        }





        return mv;

    }
}

// ///////////// 이전 코드
//@Controller
//@RequiredArgsConstructor
//@Slf4j
//public class IndexController_pre {
//
//    private final FastApiClient fastApiClient;
//
//    @GetMapping("/")
//    public String index() {
//        return "index";
//    }
//
//    @GetMapping("/recommend_hospital")
//    public ModelAndView recommend_hospital(@RequestParam("text") String text, @RequestParam("latitude") double latitude, @RequestParam("longitude") double longitude) {
//
////        FastApiClient 를 호출한다.
//        HospitalResponse hospitalList = fastApiClient.getHospital(text, latitude, longitude);
//        log.info("hospital: {}", hospitalList);
//
//        ModelAndView mv = new ModelAndView();
//        mv.setViewName("recommend_hospital");
//        mv.addObject("hospitalList", hospitalList);
//
//        return mv;
//    }



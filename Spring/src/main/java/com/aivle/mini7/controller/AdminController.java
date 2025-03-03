package com.aivle.mini7.controller;

import com.aivle.mini7.dto.LogDto;
import com.aivle.mini7.service.LogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/admin")
public class AdminController {

    private final LogService logService;

//     pageable default value
    @GetMapping("")
    public ModelAndView index(Pageable pageable) {
        ModelAndView mv = new ModelAndView();
        mv.setViewName("admin/index");

        // 1. 기존 로그 리스트 추가
        Page<LogDto.ResponseList> logList = logService.getLogList(pageable);
        mv.addObject("logList", logList);

        // 2. region 데이터 개수 추가
        List<Map<String, Object>> regionCounts = logService.getRegionCounts();
        mv.addObject("regionCounts", regionCounts);

        // 3. em_class 데이터 개수 추가
        List<Map<String, Object>> emClassCounts = logService.getEmClassCounts();
        mv.addObject("emClassCounts", emClassCounts);

        // 4. region 데이터 비율 추가 (통합 처리)
        List<Map<String, Object>> regionPercentages = logService.getRegionCountPercentages();
        for (int i = 0; i < regionCounts.size(); i++) {
            regionCounts.get(i).put("percentage", regionPercentages.get(i).get("percentage"));
        }

        // 5. em_class 데이터 비율 추가 (통합 처리)
        List<Map<String, Object>> emClassPercentages = logService.getEmClassCountPercentages();
        for (int i = 0; i < emClassCounts.size(); i++) {
            emClassCounts.get(i).put("percentage", emClassPercentages.get(i).get("percentage"));
        }

        // 6. em_class 값 1과 2의 개수 총합 추가
        int emClassOneAndTwoTotal = logService.getEmClassOneAndTwoTotal();
        mv.addObject("emClassOneAndTwoTotal", emClassOneAndTwoTotal);

//        // 7. 확장된 HospitalResponse 데이터 추가
//        LogDto.ResponseList hospitalResponse = new LogDto.ResponseList();
//        hospitalResponse.setHospitalName("Example Hospital");
//        hospitalResponse.setAddress("123 Example Street");
//        hospitalResponse.setEmergencyMedicalInstitutionType("Type A");
//        hospitalResponse.setPhoneNumber1("123-456-7890");
//        hospitalResponse.setPhoneNumber3("098-765-4321");
//        hospitalResponse.setRequest("Example Request");
//        hospitalResponse.setDistance(5.0);
//
//        HospitalResponse extendedResponse = logService.addExtraFieldsToResponse(
//                hospitalResponse, 1L, "Example Input Text", "2024-12-25 10:00:00"
//        );
//        mv.addObject("extendedResponse", extendedResponse);

        return mv;
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "admin/admin_dashboard";
    }

    @GetMapping("/login")
    public String login() {
        return "admin/admin_login";
    }
}

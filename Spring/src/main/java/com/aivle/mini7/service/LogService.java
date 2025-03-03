package com.aivle.mini7.service;

import com.aivle.mini7.client.dto.Hospital;
import com.aivle.mini7.client.dto.HospitalResponse;
import com.aivle.mini7.client.dto.HospitalResponseRecord;
import com.aivle.mini7.dto.LogDto;
import com.aivle.mini7.model.Log;
import com.aivle.mini7.repository.LogRepository;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class LogService {

    private final LogRepository logRepository;
    private final JdbcTemplate jdbcTemplate;


    @Transactional(readOnly = true)
    public Page<LogDto.ResponseList> getLogList(Pageable pageable) {
        Page<Log> logs = logRepository.findAll(pageable);

        return logs.map(LogDto.ResponseList::of);
    }

    /**
     * 원래 이렇게 나쁜 모듈로 구현하면 안된다.
     * 현재 프로젝트 완료를 위해 급급한 소스이다.
     * @param hospitalResponse
     * @param request
     * @param latitude
     * @param longitude
     */
    public void saveLog(HospitalResponse hospitalResponse, String request, double latitude, double longitude, int emClass) {
        Log hospitalLog = Log.builder()
                .inputText(request)
                .inputLatitude(latitude)
                .inputLongitude(longitude)
                .emClass(emClass)
                .datetime(String.valueOf(new Date()))
                .build();

        int count = 1;
        for(Hospital hospitalRecommend : hospitalResponse.getRecommend()) {
            log.info("hospitalResponse: {}", hospitalResponse);
            switch (count) {
                case 1:
                    hospitalLog.setHospital1(hospitalRecommend.getHospitalName());
                    hospitalLog.setAddr1(hospitalRecommend.getAddress());
                    hospitalLog.setTel1(hospitalRecommend.getPhoneNumber1());
                    hospitalLog.setRegion(hospitalRecommend.getRegion());
                    break;
                case 2:
                    hospitalLog.setHospital2(hospitalRecommend.getHospitalName());
                    hospitalLog.setAddr2(hospitalRecommend.getAddress());
                    hospitalLog.setTel2(hospitalRecommend.getPhoneNumber1());
                    hospitalLog.setRegion(hospitalRecommend.getRegion());
                    break;
                case 3:
                    hospitalLog.setHospital3(hospitalRecommend.getHospitalName());
                    hospitalLog.setAddr3(hospitalRecommend.getAddress());
                    hospitalLog.setTel3(hospitalRecommend.getPhoneNumber1());
                    hospitalLog.setRegion(hospitalRecommend.getRegion());
                    break;
            }
            count++;

        }

        logRepository.save(hospitalLog);
    }


    public void saveLogRecord(HospitalResponseRecord hospitalResponse, String request, double latitude, double longitude, int emClass) {
        Log hospitalLog = Log.builder()
                .inputText(request)
                .inputLatitude(latitude)
                .inputLongitude(longitude)
                .emClass(emClass)
                .datetime(String.valueOf(new Date()))
                .build();

        int count = 1;
        for(Hospital hospitalRecommend : hospitalResponse.getRecommend()) {
            log.info("hospitalResponse: {}", hospitalResponse);
            switch (count) {
                case 1:
                    hospitalLog.setHospital1(hospitalRecommend.getHospitalName());
                    hospitalLog.setAddr1(hospitalRecommend.getAddress());
                    hospitalLog.setTel1(hospitalRecommend.getPhoneNumber1());
                    hospitalLog.setRegion(hospitalRecommend.getRegion());
                    break;
                case 2:
                    hospitalLog.setHospital2(hospitalRecommend.getHospitalName());
                    hospitalLog.setAddr2(hospitalRecommend.getAddress());
                    hospitalLog.setTel2(hospitalRecommend.getPhoneNumber1());
                    hospitalLog.setRegion(hospitalRecommend.getRegion());
                    break;
                case 3:
                    hospitalLog.setHospital3(hospitalRecommend.getHospitalName());
                    hospitalLog.setAddr3(hospitalRecommend.getAddress());
                    hospitalLog.setTel3(hospitalRecommend.getPhoneNumber1());
                    hospitalLog.setRegion(hospitalRecommend.getRegion());
                    break;
            }
            count++;

        }

        logRepository.save(hospitalLog);
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getRegionCounts() {
        String sql = "SELECT region, COUNT(*) as count FROM log GROUP BY region";
        List<Map<String, Object>> dbResults = jdbcTemplate.query(sql, (rs, rowNum) -> {
            Map<String, Object> result = new HashMap<>();
            result.put("region", rs.getString("region"));
            result.put("count", rs.getInt("count"));
            return result;
        });

        for (int i = 0; i < dbResults.size(); i++) {
            System.out.println(dbResults.get(i));
        }

        Map<Integer, String> regionMapping = Map.of(
                1, "Seoul",
                2, "Gangwon",
                3, "Chungcheong",
                4, "Yeongnam",
                5, "Honam",
                6, "Jeju"
        );

        List<Map<String, Object>> finalResults = new ArrayList<>();
        for (int i = 1; i <= 6; i++) {
            String regionName = regionMapping.get(i);
            int count = dbResults.stream()
                    .filter(row -> row.get("region") != null && regionName.equals(row.get("region")))
                    .mapToInt(row -> (int) row.get("count"))
                    .findFirst()
                    .orElse(0);
            Map<String, Object> result = new HashMap<>();
            result.put("region", regionName);
            result.put("count", count);
            finalResults.add(result);
        }

        return finalResults;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getEmClassCounts() {
        String sql = "SELECT em_class, COUNT(*) as count FROM log GROUP BY em_class";
        List<Map<String, Object>> dbResults = jdbcTemplate.query(sql, (rs, rowNum) -> {
            Map<String, Object> result = new HashMap<>();
            result.put("em_class", rs.getInt("em_class"));
            result.put("count", rs.getInt("count"));
            return result;
        });

        List<Map<String, Object>> finalResults = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            int finalI = i;
            int count = dbResults.stream()
                    .filter(row -> (int) row.get("em_class") == finalI)
                    .mapToInt(row -> (int) row.get("count"))
                    .findFirst()
                    .orElse(0);
            Map<String, Object> result = new HashMap<>();
            result.put("em_class", i);
            result.put("count", count);
            finalResults.add(result);
        }



        return finalResults;
    }


    @Transactional(readOnly = true)
    public List<Map<String, Object>> getRegionCountPercentages() {
        List<Map<String, Object>> regionCounts = getRegionCounts();

        for (int i = 0; i < regionCounts.size(); i++) {
            System.out.println(regionCounts.get(i));
        };
        int totalRegionCount = regionCounts.stream().mapToInt(region -> (int) region.get("count")).sum();

        List<Map<String, Object>> percentages = new ArrayList<>();
        for (Map<String, Object> regionCount : regionCounts) {
            int count = (int) regionCount.get("count");
            double percentage = totalRegionCount == 0 ? 0 : ((double) count / totalRegionCount) * 100;
            Map<String, Object> result = new HashMap<>(regionCount);
            result.put("percentage", percentage);
            percentages.add(result);
        }
        return percentages;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getEmClassCountPercentages() {
        List<Map<String, Object>> emClassCounts = getEmClassCounts();
        int totalEmClassCount = emClassCounts.stream().mapToInt(emClass -> (int) emClass.get("count")).sum();

        List<Map<String, Object>> percentages = new ArrayList<>();
        for (Map<String, Object> emClassCount : emClassCounts) {
            int count = (int) emClassCount.get("count");
            double percentage = totalEmClassCount == 0 ? 0 : ((double) count / totalEmClassCount) * 100;
            Map<String, Object> result = new HashMap<>(emClassCount);
            result.put("percentage", percentage);
            percentages.add(result);
        }
        return percentages;
    }

//    public HospitalResponse addExtraFieldsToResponse(HospitalResponse hospitalResponse, Long id, String inputText, String dateTime) {
//        hospitalResponse.setId(id);
//        hospitalResponse.setInputText(inputText);
//        hospitalResponse.setDateTime(dateTime);
//        return hospitalResponse;
//    }


    @Transactional(readOnly = true)
    public int getEmClassOneAndTwoTotal() {
        String sql = "SELECT COUNT(*) FROM log WHERE em_class IN (1, 2, 3)";
        return jdbcTemplate.queryForObject(sql, Integer.class);
    }

}

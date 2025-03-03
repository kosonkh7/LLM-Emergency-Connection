package com.aivle.mini7.client.api;


import com.aivle.mini7.client.dto.HospitalResponse;
import com.aivle.mini7.client.dto.HospitalResponseRecord;
import com.aivle.mini7.client.dto.PostData;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * FastApiClient
 * @app.get("/hospital/{request}/{latitude}/{longitude}") 를 호출한다.
 */
@FeignClient(name = "fastApiClient", url = "${hospital.api.host}")
public interface FastApiClient {

     @GetMapping("/hospital_by_module")
     public HospitalResponse getHospital(@RequestParam("text") String text, @RequestParam("latitude") double latitude, @RequestParam("longitude") double longitude, @RequestParam("optionNum")int optionNum);

     @PostMapping("/hospital_by_module")
     public HospitalResponse postHospital(@ModelAttribute PostData postData);

     @PostMapping(value="/hospital_by_record", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
     public HospitalResponseRecord getHospitalWithRecord(
             @RequestPart("file") MultipartFile file,
             @RequestParam("latitude") double latitude,
             @RequestParam("longitude") double longitude
             );

}

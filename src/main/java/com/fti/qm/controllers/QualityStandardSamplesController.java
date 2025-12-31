package com.fti.qm.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fti.qm.dto.QualityStandardSampleDTO;
import com.fti.qm.services.QualityStandardSampleService;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.GridResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@Controller
@RequestMapping("/rest/qualityStandardSamplesRes")
public class QualityStandardSamplesController {

    @Autowired
    private TranslationService translationService;

    @Autowired
    private QualityStandardSampleService qualityStandardSampleService; // <-- Cần tạo Service này

    // 2.1. READ (Hiển thị Grid Data)
    @ResponseBody
    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, value = "{id}")
    public GridResponse<QualityStandardSampleDTO> findAll(@PathVariable Long id, @RequestParam String sidx, @RequestParam String sord,
                                                          @RequestParam(defaultValue = "1", required = false, value = "page") Integer page,
                                                          @RequestParam(value = "rows") int perPage, QualityStandardSampleDTO sampleDTO, HttpServletRequest request) {
        // ... Logic gọi service để lấy danh sách mẫu theo ID lệnh QC ...
        // Đảm bảo dữ liệu trả về theo format GridResponse<DTO>
        return qualityStandardSampleService.findAllByCommandId(id, sidx, sord, page, perPage, sampleDTO);
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, value = "gridConfig/{id}")
    public Map<String, Object> gridConfig(@PathVariable Long id) {
        return qualityStandardSampleService.getGridConfig(id);
    }

     @ResponseBody
     @RequestMapping(value = { "{id}" }, method = RequestMethod.PUT)
     public void update(@RequestBody QualityStandardSampleDTO qualityStandardSampleVO) {
         qualityStandardSampleService.updateResults(qualityStandardSampleVO);
     }

//    @ResponseBody
//    @RequestMapping(method = RequestMethod.GET,
//            produces = MediaType.APPLICATION_JSON_VALUE,
//            value = "/qualityEvaluationOptions")
//    public List<Map<String, String>> getQualityResultOptions() {
//        List<Map<String, String>> options = new java.util.ArrayList<>();
//
//        java.util.Map<String, String> pass = new java.util.HashMap<>();
//        pass.put("value", "01pass");
//        pass.put("key", "01pass");
//        options.add(pass);
//
//        java.util.Map<String, String> fail = new java.util.HashMap<>();
//        fail.put("value", "02fail");
//        fail.put("key", "02fail");
//        options.add(fail);
//
//        return options;
//    }

    @ResponseBody
    @RequestMapping(
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE,
            value = "/qualityEvaluationOptions"
    )
    public List<Map<String, String>> getQualityEvaluationOptions(final Locale locale) {

        List<Map<String, String>> options = new ArrayList<>();

        Map<String, String> pass = new HashMap<>();
        pass.put("value", translationService.translate(
                "qm.qualityStandardSampleRe.qualitativeResult.value.01pass", locale));
        pass.put("key", "01pass");
        options.add(pass);

        Map<String, String> fail = new HashMap<>();
        fail.put("value", translationService.translate(
                "qm.qualityStandardSampleRe.qualitativeResult.value.02fail", locale));
        fail.put("key", "02fail");
        options.add(fail);
        
        return options;
    }
}

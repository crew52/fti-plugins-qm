package com.fti.qm.controllers;

import com.fti.qm.dto.QualityStandardSampleDTO;
import com.fti.qm.services.QualityStandardSampleService;
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

    @ResponseBody
    @RequestMapping(
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE,
            value = "/qualityEvaluationOptions"
    )
    public List<Map<String, String>> getQualityEvaluationOptions(final Locale locale) {
        return qualityStandardSampleService.getQualityEvaluationOptions(locale);
    }
}

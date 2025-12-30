package com.fti.qm.controllers;

import com.fti.qm.dto.QualityStandardSampleDTO;
import com.fti.qm.services.QualityStandardSampleService;
import com.qcadoo.mes.basic.GridResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

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

    // @ResponseBody
    // @RequestMapping(value = { "{id}", "{id}.html" }, method = RequestMethod.PUT)
    // public void update(QualityStandardSampleDTO qualityStandardSampleVO) {
    //     System.out.println("=== QualityStandardSamplesController.update called, id=" + qualityStandardSampleVO.getId());
    //     qualityStandardSampleService.updateResults(qualityStandardSampleVO);
    // }

//    @ResponseBody
//    @RequestMapping(value = { "{id}", "{id}.html" }, method = RequestMethod.PUT)
//    public void update(QualityStandardSampleDTO qualityStandardSampleVO) {
//        System.out.println("=== QualityStandardSamplesController.update called, id=" + qualityStandardSampleVO.getId());
//        qualityStandardSampleService.updateResults(qualityStandardSampleVO);
//    }

    @ResponseBody
    @RequestMapping(value = { "{id}" }, method = RequestMethod.PUT)
    public void update(@PathVariable Long id,@RequestBody QualityStandardSampleDTO qualityStandardSampleVO) {
        try {
            System.out.println("=== QualityStandardSamplesController.update called, pathId=" + id + ", bodyId=" + qualityStandardSampleVO.getId());

            // Ưu tiên id từ path, để chắc chắn
            qualityStandardSampleVO.setId(id);

            qualityStandardSampleService.updateResults(qualityStandardSampleVO);

        } catch (Exception e) {
            e.printStackTrace();
            // nếu đang dùng Spring MVC cũ → để exception bubble lên cho container xử lý
            throw e;
        }
    }
}

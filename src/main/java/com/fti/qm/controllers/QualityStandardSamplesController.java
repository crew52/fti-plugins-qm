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

/**
 * REST Controller cung cấp các API thao tác với
 * mẫu thử tiêu chuẩn chất lượng (Quality Standard Sample).
 *
 * <p>
 * Controller này phục vụ cho frontend jqGrid, bao gồm:
 * </p>
 * <ul>
 *     <li>Truy vấn danh sách mẫu thử theo Lệnh kiểm tra chất lượng (QIC)</li>
 *     <li>Cung cấp cấu hình meta cho jqGrid</li>
 *     <li>Cập nhật kết quả đánh giá mẫu thử</li>
 *     <li>Cung cấp danh sách option đánh giá chất lượng (Pass / Fail)</li>
 * </ul>
 *
 * <p>
 * Base URL: <b>/rest/qualityStandardSamplesRes</b>
 * </p>
 */
@Controller
@RequestMapping("/rest/qualityStandardSamplesRes")
public class QualityStandardSamplesController {

    /**
     * Service xử lý nghiệp vụ liên quan đến mẫu thử tiêu chuẩn chất lượng.
     */
    @Autowired
    private QualityStandardSampleService qualityStandardSampleService;

    /**
     * Lấy danh sách mẫu thử chất lượng theo ID của
     * Lệnh kiểm tra chất lượng (QIC).
     *
     * <p>
     * API này được sử dụng bởi jqGrid, hỗ trợ:
     * </p>
     * <ul>
     *     <li>Phân trang</li>
     *     <li>Sắp xếp theo cột</li>
     *     <li>Lọc dữ liệu thông qua các tham số tìm kiếm</li>
     * </ul>
     *
     * <p>
     * Dữ liệu trả về theo chuẩn {@link GridResponse} để jqGrid
     * có thể hiển thị trực tiếp.
     * </p>
     *
     * @param id         ID của Quality Inspection Command
     * @param sidx       Tên cột dùng để sắp xếp
     * @param sord       Thứ tự sắp xếp (asc | desc)
     * @param page       Trang hiện tại (mặc định: 1)
     * @param perPage    Số bản ghi trên mỗi trang
     * @param sampleDTO  Đối tượng chứa điều kiện filter từ jqGrid
     * @param request    HTTP request (không sử dụng trực tiếp, phục vụ mở rộng)
     *
     * @return {@link GridResponse} chứa danh sách {@link QualityStandardSampleDTO}
     */
    @ResponseBody
    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, value = "{id}")
    public GridResponse<QualityStandardSampleDTO> findAll(@PathVariable Long id, @RequestParam String sidx, @RequestParam String sord,
                                                          @RequestParam(defaultValue = "1", required = false, value = "page") Integer page,
                                                          @RequestParam(value = "rows") int perPage, QualityStandardSampleDTO sampleDTO, HttpServletRequest request) {
        // ... Logic gọi service để lấy danh sách mẫu theo ID lệnh QC ...
        // Đảm bảo dữ liệu trả về theo format GridResponse<DTO>
        return qualityStandardSampleService.findAllByCommandId(id, sidx, sord, page, perPage, sampleDTO);
    }

    /**
     * Lấy cấu hình meta cho jqGrid hiển thị danh sách mẫu thử chất lượng.
     *
     * <p>
     * Cấu hình bao gồm:
     * </p>
     * <ul>
     *     <li>Danh sách cột</li>
     *     <li>Trạng thái read-only của grid</li>
     * </ul>
     *
     * <p>
     * Trạng thái read-only phụ thuộc vào trạng thái của
     * Lệnh kiểm tra chất lượng (QIC).
     * </p>
     *
     * @param id ID của Quality Inspection Command
     *
     * @return Map chứa cấu hình meta cho jqGrid
     */
    @ResponseBody
    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, value = "gridConfig/{id}")
    public Map<String, Object> gridConfig(@PathVariable Long id) {
        return qualityStandardSampleService.getGridConfig(id);
    }

    /**
     * Cập nhật kết quả đánh giá của một mẫu thử chất lượng.
     *
     * <p>
     * API này được gọi khi người dùng chỉnh sửa dữ liệu
     * trực tiếp trên jqGrid (inline edit).
     * </p>
     *
     * <p>
     * Các thông tin được cập nhật bao gồm:
     * </p>
     * <ul>
     *     <li>Kết quả định tính</li>
     *     <li>Kết quả định lượng</li>
     *     <li>Đánh giá định lượng</li>
     * </ul>
     *
     * @param qualityStandardSampleVO DTO chứa dữ liệu cần cập nhật
     */
     @ResponseBody
     @RequestMapping(value = { "{id}" }, method = RequestMethod.PUT)
     public void update(@RequestBody QualityStandardSampleDTO qualityStandardSampleVO) {
         qualityStandardSampleService.updateResults(qualityStandardSampleVO);
     }

    /**
     * Lấy danh sách option đánh giá chất lượng
     * (Pass / Fail) để hiển thị cho dropdown hoặc select box.
     *
     * <p>
     * Giá trị hiển thị đã được dịch theo locale hiện tại.
     * </p>
     *
     * @param locale Locale hiện tại
     *
     * @return Danh sách option dạng key/value
     */
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

package com.fti.qm.services;

import com.fti.qm.dto.QualityStandardSampleDTO;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.GridResponse;
import com.qcadoo.mes.basic.LookupUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
public class QualityStandardSampleService {

    public static final String QIC_ID = "qicId";

    public static final String ID = "id";

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private LookupUtils lookupUtils;

    @Autowired
    private TranslationService translationService;

    /**
     * Tìm tất cả các mẫu thử theo ID của Lệnh kiểm tra chất lượng (QIC)
     */
    public GridResponse<QualityStandardSampleDTO> findAllByCommandId(final Long qicId, final String _sidx, final String _sord,
                                                          int page, int perPage, final QualityStandardSampleDTO sampleSearch) {

        String sidx = _sidx != null ? _sidx : "";
        String sord = _sord != null ? _sord : "";

        Preconditions.checkState(Arrays.asList("asc", "desc", "").contains(sord.toLowerCase()));

        // 1. Xây dựng câu truy vấn SQL (Dựa trên bảng sinh ra từ model qualityStandardSampleRe)
        // Tên bảng thường là pluginName_modelName (ví dụ: qm_qualitystandardsamplere)
//        String query = "SELECT %s FROM ( "
//                + " SELECT s.id, s.qualityinspectioncommandre_id AS qualityInspectionCommandRe, "
//                + " s.qualityStandardL_id AS qualityStandardL, "
//                + " s.samplenumber AS sampleNumber, s.qualitativeresult AS qualitativeResult, "
//                + " s.quantitativeresult AS quantitativeResult, s.quantitativeevaluation AS quantitativeEvaluation "
//                + " FROM qm_qualityStandardSampleRe s "
//                + " WHERE s.qualityinspectioncommandre_id = :qicId "
//                + ") q ";

        String query = "SELECT %s FROM ( "
                + " SELECT "
                + "   s.id AS id, "
                + "   c.id AS qualityInspectionCommandRe, "
                + "   l.id AS qualityStandardL, "
                + "   l.position AS position, "
                + "   qc.number AS qcNumber, "
                + "   qc.name AS qcName, "
                + "   qc.type AS qcType, "
                + "   l.unit AS unit, "
                + "   l.description AS description, "
                + "   l.samplesize AS sampleSize, "
                + "   s.samplenumber AS sampleNumber, "
                + "   s.qualitativeresult AS qualitativeResult, "
                + "   l.quantitativevalue AS quantitativeValue, "
                + "   l.upvalue AS upValue, "
                + "   l.downvalue AS downValue, "
                + "   s.quantitativeresult AS quantitativeResult, "
                + "   s.quantitativeevaluation AS quantitativeEvaluation, "
                + "   me.name AS meName, "
                + "   me.measuringmethod AS meMeasuringMethod "
                + " FROM qm_qualitystandardsamplere s "
                + " JOIN qm_qualitystandardl l ON s.qualitystandardl_id = l.id "
                + " JOIN qm_qualityinspectioncommandre c ON s.qualityinspectioncommandre_id = c.id "
                + " LEFT JOIN qm_qualitycriteria qc ON l.qualitycriteria_id = qc.id "
                + " LEFT JOIN qm_measuringequipment me ON l.measuringequipment_id = me.id "
                + " WHERE s.qualityinspectioncommandre_id = :qicId "
                + "   AND s.deleted = false "
                + ") q ";

        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put("qicId", qicId);

        // 2. Thêm tính năng lọc (Filter) từ grid nếu có
        // lookupUtils giúp tự động tạo câu lệnh WHERE từ đối tượng search
        String whereClause = lookupUtils.addQueryWhereForObject(sampleSearch);
        query += whereClause;
        parameters.putAll(lookupUtils.getParametersForObject(sampleSearch));

        // 3. Đếm tổng số bản ghi
        String queryCount = String.format(query, "COUNT(*)");
        int countRecords = jdbcTemplate.queryForObject(queryCount, parameters, Integer.class);

        // 4. Truy vấn dữ liệu phân trang và sắp xếp
        String queryRecords = String.format(query, "*")
                + String.format(" ORDER BY q.%s %s LIMIT %d OFFSET %d", sidx, sord, perPage, perPage * (page - 1));

        List<QualityStandardSampleDTO> records = jdbcTemplate.query(queryRecords, parameters, (resultSet, i) -> {
            QualityStandardSampleDTO dto = new QualityStandardSampleDTO();
            dto.setId(resultSet.getLong("id"));
            dto.setQualityInspectionCommandRe(resultSet.getLong("qualityInspectionCommandRe"));
            dto.setQualityStandardL(resultSet.getLong("qualityStandardL"));
            dto.setPosition(resultSet.getString("position"));
            dto.setQcNumber(resultSet.getString("qcNumber"));
            dto.setQcName(resultSet.getString("qcName"));
            String qcType = resultSet.getString("qcType");

            if (qcType != null) {
                String key = "qm.qualityCriteria.type.value." + qcType;
                dto.setQcType(
                        translationService.translate(
                                key,
                                LocaleContextHolder.getLocale()
                        )
                );
            } else {
                dto.setQcType(null);
            }
            dto.setUnit(resultSet.getString("unit"));
            dto.setDescription(resultSet.getString("description"));
            dto.setSampleSize(resultSet.getInt("sampleSize"));
            dto.setSampleNumber(resultSet.getInt("sampleNumber"));
            dto.setQualitativeResult(resultSet.getString("qualitativeResult"));
            dto.setQuantitativeValue(resultSet.getBigDecimal("quantitativeValue"));
            dto.setUpValue(resultSet.getBigDecimal("upValue"));
            dto.setDownValue(resultSet.getBigDecimal("downValue"));
            dto.setQuantitativeResult(resultSet.getBigDecimal("quantitativeResult"));
            dto.setQuantitativeEvaluation(resultSet.getString("quantitativeEvaluation"));
            dto.setMeName(resultSet.getString("meName"));
            dto.setMeMeasuringMethod(resultSet.getString("meMeasuringMethod"));
            return dto;
        });

        // 5. Trả về kết quả theo định dạng GridResponse cho jqGrid
        return new GridResponse<>(page, (int) Math.ceil((double) countRecords / perPage), countRecords, records);
    }
}

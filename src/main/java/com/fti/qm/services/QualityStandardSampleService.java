package com.fti.qm.services;

import com.fti.qm.dto.QualityStandardSampleDTO;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.GridResponse;
import com.qcadoo.mes.basic.LookupUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

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

        String query = "SELECT %s FROM ( "
                + " SELECT "
                + "   ROW_NUMBER() OVER (ORDER BY s.id) AS number, "
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
                + "   l.qualitativeValue AS qualitativeValue, "
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
            dto.setNumber(resultSet.getLong("number"));
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
            String qualitativeValue = resultSet.getString("qualitativeValue");

            if (qualitativeValue != null) {
                String key = "qm.qualityStandardL.qualitativeValue.value." + qualitativeValue;
                dto.setQualitativeValue(
                        translationService.translate(
                                key,
                                LocaleContextHolder.getLocale()
                        )
                );
            } else {
                dto.setQualitativeValue(null);
            }
            dto.setSampleSize(resultSet.getInt("sampleSize"));
            dto.setSampleNumber(resultSet.getInt("sampleNumber"));
            String qualitativeResult = resultSet.getString("qualitativeResult");
            if (qualitativeResult != null) {
                String key = "qm.qualityStandardSampleRe.qualitativeResult.value." + qualitativeResult;
                dto.setQualitativeResult(
                        translationService.translate(
                                key,
                                LocaleContextHolder.getLocale()
                        )
                );
            } else {
                dto.setQualitativeResult(null);
            }
            dto.setQuantitativeValue(resultSet.getBigDecimal("quantitativeValue"));
            dto.setUpValue(resultSet.getBigDecimal("upValue"));
            dto.setDownValue(resultSet.getBigDecimal("downValue"));
            dto.setQuantitativeResult(resultSet.getBigDecimal("quantitativeResult"));
            String quantitativeEvaluation = resultSet.getString("quantitativeEvaluation");
            if (quantitativeEvaluation != null) {
                String key = "qm.qualityStandardSampleRe.quantitativeEvaluation.value." + quantitativeEvaluation;
                dto.setQuantitativeEvaluation(
                        translationService.translate(
                                key,
                                LocaleContextHolder.getLocale()
                        )
                );
            } else {
                dto.setQuantitativeEvaluation(null);
            }
            dto.setMeName(resultSet.getString("meName"));
            dto.setMeMeasuringMethod(resultSet.getString("meMeasuringMethod"));
            return dto;
        });

        // 5. Trả về kết quả theo định dạng GridResponse cho jqGrid
        return new GridResponse<>(page, (int) Math.ceil((double) countRecords / perPage), countRecords, records);
    }

    /**
     * Lấy cấu hình Grid theo chuẩn Meta-config của DocumentPosition
     */
    public Map<String, Object> getGridConfig(final Long qicId) {
        Map<String, Object> config = Maps.newHashMap();
        List<Map<String, Object>> columns = Lists.newArrayList();

        // 1. Định nghĩa danh sách cột (Chỉ trả về meta-data, không trả về style hiển thị)
        // Cấu trúc: name, checked, forAttribute, attributeDataType, attributeValueType

        columns.add(createColumn("act", true)); // Cột action (edit/delete)
        columns.add(createColumn("number", true));
        columns.add(createColumn("position", true));
        columns.add(createColumn("qcNumber", true));
        columns.add(createColumn("qcName", true));
        columns.add(createColumn("qcType", true));
        columns.add(createColumn("unit", true));
        columns.add(createColumn("description", true));
        columns.add(createColumn("sampleSize", true));
        columns.add(createColumn("sampleNumber", true));
        columns.add(createColumn("qualitativeValue", true));
        columns.add(createColumn("qualitativeResult", true));
        columns.add(createColumn("quantitativeValue", true)); // Giá trị chuẩn
        columns.add(createColumn("upValue", true));
        columns.add(createColumn("downValue", true));
        columns.add(createColumn("quantitativeResult", true)); // Kết quả thực tế
        columns.add(createColumn("quantitativeEvaluation", true)); // Đánh giá
        columns.add(createColumn("meName", true));
        columns.add(createColumn("meMeasuringMethod", true));

        config.put("columns", columns);

        // 2. Các tham số điều khiển hành vi của Grid (Giống DocumentPosition)
        config.put("readOnly", isReadOnly(qicId));

        return config;
    }

    /**
     * Hàm hỗ trợ tạo cấu hình cột chuẩn Meta-config
     */
    private Map<String, Object> createColumn(String name, boolean checked) {
        Map<String, Object> col = Maps.newHashMap();
        col.put("name", name);
        col.put("checked", checked);
        col.put("forAttribute", false);
        col.put("attributeDataType", null);
        col.put("attributeValueType", null);
        return col;
    }

    /**
     * Kiểm tra xem Grid có ở chế độ chỉ đọc hay không (Dựa vào trạng thái QIC)
     */
    private boolean isReadOnly(final Long qicId) {
        String query = "SELECT status FROM qm_qualityinspectioncommandre WHERE id = :qicId";
        Map<String, Object> params = Maps.newHashMap();
        params.put("qicId", qicId);
        try {
            String status = jdbcTemplate.queryForObject(query, params, String.class);
            // Nếu trạng thái là '03completed' hoặc '01new' thì khóa grid không cho sửa
            return "03completed".equals(status) || "01new".equals(status);
        } catch (Exception e) {
            return false;
        }
    }

    public void updateResults(final QualityStandardSampleDTO dto) {
        Preconditions.checkNotNull(dto.getId(), "Sample id must not be null");

        StringBuilder setClause = new StringBuilder();
        Map<String, Object> params = Maps.newHashMap();

        // qualitativeResult
        setClause.append("qualitativeresult = :qualitativeResult, ");
        params.put("qualitativeResult", dto.getQualitativeResult());

        // quantitativeResult
        setClause.append("quantitativeresult = :quantitativeResult, ");
        params.put("quantitativeResult", dto.getQuantitativeResult());

        // quantitativeEvaluation
        setClause.append("quantitativeevaluation = :quantitativeEvaluation ");
        params.put("quantitativeEvaluation", dto.getQuantitativeEvaluation());

        params.put(ID, dto.getId());

        String sql = "UPDATE qm_qualitystandardsamplere "
                + "SET " + setClause.toString()
                + "WHERE id = :id";

        jdbcTemplate.update(sql, params);
    }

    public List<Map<String, String>> getQualityEvaluationOptions(final Locale locale) {

        List<Map<String, String>> options = new ArrayList<>();

        options.add(createOption(
                "01pass",
                "qm.qualityStandardSampleRe.qualitativeResult.value.01pass",
                locale
        ));

        options.add(createOption(
                "02fail",
                "qm.qualityStandardSampleRe.qualitativeResult.value.02fail",
                locale
        ));

        return options;
    }

    private Map<String, String> createOption(
            final String value,
            final String translationKey,
            final Locale locale) {

        Map<String, String> option = new HashMap<>();
        option.put("key", value); // value lưu DB
        option.put("value", translationService.translate(translationKey, locale));
        return option;
    }

}

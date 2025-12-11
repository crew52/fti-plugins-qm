package com.fti.qm.imports.eQSL;

import com.fti.qm.constants.QMConstants;
import com.fti.qm.constants.QSHFields;
import com.fti.qm.constants.QSLFields;
import com.qcadoo.mes.basic.imports.services.XlsxImportService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Service xử lý import Excel cho eQSL, gồm:
 * - Gán QualityStandardH dựa trên Tool Number & Inspection Type.
 * - Tự động tính Up/Down Value nếu chưa có.
 */
@Service
public class EQSLXlsxImportService extends XlsxImportService {

    private static final String L_QCADOO_VIEW_VALIDATE_FIELD_ERROR_LOOKUP_CODE_NOT_FOUND =
            "qcadooView.validate.field.error.lookupCodeNotFound";

    @Autowired
    protected DataDefinitionService dataDefinitionService;

    /**
     * Validate bản ghi import:
     * - Gán QUALITY_STANDARD_H.
     * - Tính UP/DOWN_VALUE nếu cần.
     */
    @Override
    public void validateEntity(Entity entity, DataDefinition dataDefinition) {
        if (!assignQualityStandardH(entity, dataDefinition)) {
            return;
        }
        calculateUpDownValue(entity);

    }

    /**
     * Tìm và gán QUALITY_STANDARD_H từ TOOL_NUMBER và INSPECTION_TYPE.
     *
     * @return true nếu tìm thấy và gán thành công; false nếu lỗi.
     */
    private boolean assignQualityStandardH(Entity entity, DataDefinition dataDefinition) {

        String toolNumber = entity.getStringField(QSLFields.IMPORT_TOOL_NUMBER);
        String inspectionType = entity.getStringField(QSLFields.IMPORT_INSPECTION_TYPE);

        // Tìm QSH
        Entity qsh = findActiveQSHByToolNumberAndInspectionType(toolNumber, inspectionType);

        if (qsh == null) {
            entity.addError(
                    dataDefinition.getField(QSLFields.IMPORT_TOOL_NUMBER),
                    L_QCADOO_VIEW_VALIDATE_FIELD_ERROR_LOOKUP_CODE_NOT_FOUND
            );
            return false;
        }

        // Gán vào field QSL.QUALITY_STANDARD_H
        entity.setField(QSLFields.QUALITY_STANDARD_H, qsh);

        return true;
    }

    /**
     * Tìm QSH đang active theo Tool Number và Inspection Type.
     *
     * @return entity QSH nếu tìm thấy, ngược lại null.
     */
    private Entity findActiveQSHByToolNumberAndInspectionType(final String toolNumber, final String inspectionType) {
        if (toolNumber == null || toolNumber.trim().isEmpty()) {
            return null;
        }

        return getQualityStandardHDD().find()
                .createAlias(QSHFields.TOOL, "t")
                .add(SearchRestrictions.eq("t.number", toolNumber))
                .add(SearchRestrictions.eq(QSHFields.INSPECTION_TYPE, inspectionType))
                .add(SearchRestrictions.eq(QSHFields.TYPE, QSHFields.Type.EQUIPMENT))
                .add(SearchRestrictions.eq(QSHFields.ACTIVE, true))
                .add(SearchRestrictions.eq(QSHFields.DELETED, false))
                .setMaxResults(1)
                .uniqueResult();
    }

    /**
     * Lấy DataDefinition của bảng qualityStandardH.
     */
    private DataDefinition getQualityStandardHDD() {
        return dataDefinitionService.get(
                QMConstants.PLUGIN_IDENTIFIER,
                QMConstants.MODEL_QUALITY_STANDARD_H
        );
    }

    /**
     * Tính UP_VALUE và DOWN_VALUE:
     * - Bỏ qua nếu thiếu QUANTITATIVE_VALUE hoặc TOLERANCE.
     * - Giữ nguyên nếu user đã nhập sẵn.
     * - Nếu đều null → tự tính: UP = Q + T, DOWN = Q - T.
     */
    private void calculateUpDownValue(Entity entity) {
        BigDecimal quantitativeValue = entity.getDecimalField(QSLFields.QUANTITATIVE_VALUE);
        BigDecimal tolerance = entity.getDecimalField(QSLFields.TOLERANCE);
        BigDecimal upValue = entity.getDecimalField(QSLFields.UP_VALUE);
        BigDecimal downValue = entity.getDecimalField(QSLFields.DOWN_VALUE);

        // TH3: Thiếu data => bỏ qua
        if (quantitativeValue == null || tolerance == null) {
            return;
        }

        // TH2: User nhập sẵn rồi => giữ nguyên
        if (upValue != null && downValue != null) {
            return;
        }

        // TH1: Tự tính
        if (upValue == null && downValue == null) {
            entity.setField(QSLFields.UP_VALUE, quantitativeValue.add(tolerance));
            entity.setField(QSLFields.DOWN_VALUE, quantitativeValue.subtract(tolerance));
        }
    }
}

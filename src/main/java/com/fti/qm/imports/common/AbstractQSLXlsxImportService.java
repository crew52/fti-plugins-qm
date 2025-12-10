package com.fti.qm.imports.common;

import com.fti.qm.constants.QSLFields;
import com.qcadoo.mes.basic.imports.services.XlsxImportService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

import java.math.BigDecimal;

/**
 * Lớp dùng chung cho xử lý tính toán QSL (quality standard line).
 * IQSL và OQSL chỉ cần kế thừa mà không phải lặp lại code.
 */
public abstract class AbstractQSLXlsxImportService extends XlsxImportService {

    @Override
    public void validateEntity(Entity entity, DataDefinition dataDefinition) {

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

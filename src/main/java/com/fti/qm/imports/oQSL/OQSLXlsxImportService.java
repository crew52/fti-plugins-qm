package com.fti.qm.imports.oQSL;

import com.fti.qm.constants.QSLFields;
import com.qcadoo.mes.basic.imports.services.XlsxImportService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class OQSLXlsxImportService extends XlsxImportService {
    @Override
    public void validateEntity(Entity entity, DataDefinition dataDefinition) {
        BigDecimal quantitativeValue = entity.getDecimalField(QSLFields.QUANTITATIVE_VALUE);
        BigDecimal tolerance = entity.getDecimalField(QSLFields.TOLERANCE);
        BigDecimal upValue = entity.getDecimalField(QSLFields.UP_VALUE);
        BigDecimal downValue = entity.getDecimalField(QSLFields.DOWN_VALUE);

        // TRƯỜNG HỢP 3: Nếu thiếu 1 trong 2 thì bỏ qua
        if (quantitativeValue == null || tolerance == null) {
            return;
        }

        // TRƯỜNG HỢP 2: upValue & downValue đã có => không làm gì
        if (upValue != null && downValue != null) {
            return;
        }

        // TRƯỜNG HỢP 1: Tự động tính toán
        if (upValue == null && downValue == null) {
            entity.setField(QSLFields.UP_VALUE, quantitativeValue.add(tolerance));
            entity.setField(QSLFields.DOWN_VALUE, quantitativeValue.subtract(tolerance));
        }
    }
}

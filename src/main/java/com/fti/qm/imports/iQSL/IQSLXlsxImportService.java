package com.fti.qm.imports.iQSL;

import com.qcadoo.mes.basic.imports.services.XlsxImportService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class IQSLXlsxImportService extends XlsxImportService {
    @Override
    public void validateEntity(Entity entity, DataDefinition dataDefinition) {
        BigDecimal quantitativeValue = entity.getDecimalField("quantitativeValue");
        BigDecimal tolerance = entity.getDecimalField("tolerance");
        BigDecimal upValue = entity.getDecimalField("upValue");
        BigDecimal downValue = entity.getDecimalField("downValue");

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
            entity.setField("upValue", quantitativeValue.add(tolerance));
            entity.setField("downValue", quantitativeValue.subtract(tolerance));
        }
    }
}

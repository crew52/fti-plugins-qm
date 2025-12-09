package com.fti.qm.imports.measuringEquipment;

import com.fti.qm.constants.MeasuringEquipmentFields;
import com.fti.qm.constants.QSLFields;
import com.qcadoo.mes.basic.imports.services.XlsxImportService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.FieldDefinition;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class MeasuringEquipmentXlsxImportService extends XlsxImportService{
    @Override
    public void validateEntity(Entity entity, DataDefinition dataDefinition) {
        validateNumber(entity, dataDefinition);
    }

    /**
     * Kiểm tra trường number: phải là số và đúng 6 ký tự.
     */
    private void validateNumber(Entity entity, DataDefinition dataDefinition) {
        String number = entity.getStringField(MeasuringEquipmentFields.NUMBER);

        // Nếu null hoặc rỗng: để model tự báo lỗi required
        if (number == null || number.trim().isEmpty()) {
            return;
        }

        number = number.trim();
        FieldDefinition field = dataDefinition.getField(MeasuringEquipmentFields.NUMBER);

        // Chỉ cho phép ký tự số
        if (!number.matches("\\d+")) {
            entity.addError(field, "qcadoo.validate.fieldError.notNumeric");
            return;
        }

        // Đúng 6 ký tự
        if (number.length() != 6) {
            entity.addError(field, "qcadoo.validate.fieldError.invalidLength");
        }

        // Không cho phép toàn số 0
        if ("000000".equals(number)) {
            entity.addError(field, "qcadoo.validate.fieldError.invalidNumber");
        }
    }
}

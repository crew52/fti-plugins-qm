package com.fti.qm.hooks;

import com.fti.qm.constants.MeasuringEquipmentFields;
import com.fti.qm.constants.QMConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.utils.NumberGeneratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MeasuringEquipmentHooks {

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    public void onCreate(final DataDefinition dd, final Entity entity) {
        // Sinh tự động khi record được tạo (trước khi validate)
        String number = entity.getStringField(MeasuringEquipmentFields.NUMBER);
        if (number == null || number.trim().isEmpty()) {
            String generated = numberGeneratorService.generateNumber(
                    QMConstants.PLUGIN_IDENTIFIER,
                    QMConstants.MODEL_MEASURING_EQUIPMENT
            );
            entity.setField(MeasuringEquipmentFields.NUMBER, generated);
        }
    }
}

package com.fti.qm.hooks;

import com.fti.qm.constants.GlobalFields;
import com.fti.qm.constants.MeasuringEquipmentFields;
import com.fti.qm.constants.QMConstants;
import com.fti.qm.helpers.SoftDeleteHelper;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.utils.NumberGeneratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MeasuringEquipmentHooks {

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    @Autowired
    private SoftDeleteHelper softDeleteHelper;

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

    public void onUpdate(final DataDefinition dataDefinition, final Entity measuringEquipment) {
        Boolean deleted = measuringEquipment.getBooleanField(GlobalFields.DELETED);
        if (deleted == null || !deleted) {
            return;
        }

        // Xóa mềm các bản ghi con
        String joinField = QMConstants.MODEL_MEASURING_EQUIPMENT;
        softDeleteHelper.softDeleteRelatedLines(QMConstants.MODEL_INCOMING_QUALITY_STANDARD_L, joinField, measuringEquipment);
        softDeleteHelper.softDeleteRelatedLines(QMConstants.MODEL_OUTGOING_QUALITY_STANDARD_L, joinField, measuringEquipment);
        softDeleteHelper.softDeleteRelatedLines(QMConstants.MODEL_IN_PROCESS_QUALITY_STANDARD_L, joinField, measuringEquipment);
        softDeleteHelper.softDeleteRelatedLines(QMConstants.MODEL_EQUIPMENT_QUALITY_STANDARD_L, joinField, measuringEquipment);
    }
}

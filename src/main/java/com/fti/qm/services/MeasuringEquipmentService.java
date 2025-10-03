package com.fti.qm.services;

import com.fti.qm.constants.MeasuringEquipmentFields;
import com.fti.qm.constants.QMConstants;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.utils.NumberGeneratorService;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MeasuringEquipmentService {
    @Autowired
    private NumberGeneratorService numberGeneratorService;

    public void onBeforeRender(final ViewDefinitionState view) {
        generateEmployeeNumber(view);
    }

    public void generateEmployeeNumber(final ViewDefinitionState view) {
        numberGeneratorService.generateAndInsertNumber(view, QMConstants.PLUGIN_IDENTIFIER, QMConstants.MODEL_MEASURING_EQUIPMENT,
                QcadooViewConstants.L_FORM, MeasuringEquipmentFields.NUMBER);
    }
}

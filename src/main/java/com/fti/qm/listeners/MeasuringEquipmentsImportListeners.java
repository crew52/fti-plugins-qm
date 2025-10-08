package com.fti.qm.listeners;

import com.fti.qm.constants.MeasuringEquipmentFields;
import com.fti.qm.constants.QMConstants;
import com.fti.qm.imports.measuringEquipment.MeasuringEquipmentCellBinderRegistry;
import com.fti.qm.imports.measuringEquipment.MeasuringEquipmentXlsxImportService;
import com.qcadoo.mes.basic.imports.services.XlsxImportService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriterion;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class MeasuringEquipmentsImportListeners {
    @Autowired
    private MeasuringEquipmentXlsxImportService measuringEquipmentXlsxImportService;

    @Autowired
    private MeasuringEquipmentCellBinderRegistry measuringEquipmentCellBinderRegistry;

    public void processImportFile(final ViewDefinitionState view, final ComponentState state, final String[] args)
            throws IOException {
        measuringEquipmentXlsxImportService.processImportFile(view, measuringEquipmentCellBinderRegistry.getCellBinderRegistry(), true,
                QMConstants.PLUGIN_IDENTIFIER, QMConstants.MODEL_MEASURING_EQUIPMENT, MeasuringEquipmentsImportListeners::createRestrictionForMeasuringEquipment);
    }

    private static SearchCriterion createRestrictionForMeasuringEquipment(final Entity measuringEquipment) {
        return SearchRestrictions.eq(MeasuringEquipmentFields.NUMBER, measuringEquipment.getStringField(MeasuringEquipmentFields.NUMBER));
    }

    public void downloadImportSchema(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        measuringEquipmentXlsxImportService.downloadImportSchema(view, QMConstants.PLUGIN_IDENTIFIER,
                QMConstants.MODEL_MEASURING_EQUIPMENT, XlsxImportService.L_XLSX);
    }
    
    public void redirectToLogs(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        measuringEquipmentXlsxImportService.redirectToLogs(view, QMConstants.MODEL_MEASURING_EQUIPMENT);
    }

     public void onInputChange(final ViewDefinitionState view, final ComponentState state, final String[] args) {
         measuringEquipmentXlsxImportService.changeButtonsState(view, false);
     }
}

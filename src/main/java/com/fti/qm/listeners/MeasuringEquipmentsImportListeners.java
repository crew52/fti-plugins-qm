package com.fti.qm.listeners;

import com.fti.qm.constants.QMConstants;
import com.fti.qm.imports.measuringEquipment.MeasuringEquipmentXlsxImportService;
import com.qcadoo.mes.basic.imports.services.XlsxImportService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MeasuringEquipmentsImportListeners {
    @Autowired
    private MeasuringEquipmentXlsxImportService measuringEquipmentXlsxImportService;

    public void downloadImportSchema(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        measuringEquipmentXlsxImportService.downloadImportSchema(view, QMConstants.PLUGIN_IDENTIFIER,
                QMConstants.MODEL_MEASURING_EQUIPMENT, XlsxImportService.L_XLSX);
    }
}

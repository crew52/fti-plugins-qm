package com.fti.qm.listeners.qSImport;

import com.fti.qm.constants.QMConstants;
import com.fti.qm.imports.oQSL.OQSLCellBinderRegistry;
import com.fti.qm.imports.oQSL.OQSLXlsxImportService;
import com.qcadoo.mes.basic.imports.services.XlsxImportService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class OQSLsImportListeners {
    @Autowired
    private OQSLXlsxImportService oqslXlsxImportService;
    @Autowired
    private OQSLCellBinderRegistry oqslCellBinderRegistry;

    public OQSLsImportListeners() {
    }

    public void downloadImportSchema(ViewDefinitionState view, ComponentState state, String[] args) {
        this.oqslXlsxImportService.downloadImportSchema(view, QMConstants.PLUGIN_IDENTIFIER, "qSLProduct", XlsxImportService.L_XLSX);
    }

    public void processImportFile(ViewDefinitionState view, ComponentState state, String[] args) throws IOException {
        this.oqslXlsxImportService.processImportFile(view, this.oqslCellBinderRegistry.getCellBinderRegistry(), true, QMConstants.PLUGIN_IDENTIFIER, QMConstants.MODEL_QUALITY_STANDARD_L);
    }

    public void redirectToLogs(ViewDefinitionState view, ComponentState state, String[] args) {
        this.oqslXlsxImportService.redirectToLogs(view, QMConstants.MODEL_QUALITY_STANDARD_L);
    }

    public void onInputChange(ViewDefinitionState view, ComponentState state, String[] args) {
        this.oqslXlsxImportService.changeButtonsState(view, false);
    }
}

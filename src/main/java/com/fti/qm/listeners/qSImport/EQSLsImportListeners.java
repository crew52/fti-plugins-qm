package com.fti.qm.listeners.qSImport;

import com.fti.qm.constants.QMConstants;
import com.fti.qm.imports.eQSL.EQSLCellBinderRegistry;
import com.fti.qm.imports.eQSL.EQSLXlsxImportService;
import com.fti.qm.imports.iQSL.IQSLCellBinderRegistry;
import com.fti.qm.imports.iQSL.IQSLXlsxImportService;
import com.qcadoo.mes.basic.imports.services.XlsxImportService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class EQSLsImportListeners {
    @Autowired
    private EQSLXlsxImportService eqslXlsxImportService;
    @Autowired
    private EQSLCellBinderRegistry eqslCellBinderRegistry;

    public EQSLsImportListeners() {
    }

    public void downloadImportSchema(ViewDefinitionState view, ComponentState state, String[] args) {
        this.eqslXlsxImportService.downloadImportSchema(view, QMConstants.PLUGIN_IDENTIFIER, "eQSL", XlsxImportService.L_XLSX);
    }

    public void processImportFile(ViewDefinitionState view, ComponentState state, String[] args) throws IOException {
        this.eqslXlsxImportService.processImportFile(view, this.eqslCellBinderRegistry.getCellBinderRegistry(), true, QMConstants.PLUGIN_IDENTIFIER, QMConstants.MODEL_QUALITY_STANDARD_L);
    }

    public void redirectToLogs(ViewDefinitionState view, ComponentState state, String[] args) {
        this.eqslXlsxImportService.redirectToLogs(view, QMConstants.MODEL_QUALITY_STANDARD_L);
    }

    public void onInputChange(ViewDefinitionState view, ComponentState state, String[] args) {
        this.eqslXlsxImportService.changeButtonsState(view, false);
    }
}

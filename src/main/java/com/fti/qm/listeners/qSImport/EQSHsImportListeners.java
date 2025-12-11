package com.fti.qm.listeners.qSImport;

import com.fti.qm.constants.QMConstants;
import com.fti.qm.imports.eQSH.EQSHCellBinderRegistry;
import com.fti.qm.imports.eQSH.EQSHXlsxImportService;
import com.qcadoo.mes.basic.imports.services.XlsxImportService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class EQSHsImportListeners {
    @Autowired
    private EQSHXlsxImportService eqshXlsxImportService;
    @Autowired
    private EQSHCellBinderRegistry eqshCellBinderRegistry;

    public EQSHsImportListeners() {
    }

    public void downloadImportSchema(ViewDefinitionState view, ComponentState state, String[] args) {
        this.eqshXlsxImportService.downloadImportSchema(view, QMConstants.PLUGIN_IDENTIFIER, "eQSH", XlsxImportService.L_XLSX);
    }

    public void processImportFile(ViewDefinitionState view, ComponentState state, String[] args) throws IOException {
        this.eqshXlsxImportService.processImportFile(view, this.eqshCellBinderRegistry.getCellBinderRegistry(), true, QMConstants.PLUGIN_IDENTIFIER, QMConstants.MODEL_QUALITY_STANDARD_H);
    }

    public void redirectToLogs(ViewDefinitionState view, ComponentState state, String[] args) {
        this.eqshXlsxImportService.redirectToLogs(view, QMConstants.MODEL_QUALITY_STANDARD_H);
    }

    public void onInputChange(ViewDefinitionState view, ComponentState state, String[] args) {
        this.eqshXlsxImportService.changeButtonsState(view, false);
    }
}

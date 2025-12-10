package com.fti.qm.listeners.qSImport;

import com.fti.qm.constants.QMConstants;
import com.fti.qm.imports.qshProduct.OQSHXlsxImportService;
import com.fti.qm.imports.qshProduct.QSHProductCellBinderRegistry;
import com.qcadoo.mes.basic.imports.services.XlsxImportService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class OQSHsImportListeners {

    @Autowired
    private OQSHXlsxImportService oqshXlsxImportService;
    @Autowired
    private QSHProductCellBinderRegistry oqshCellBinderRegistry;

    public OQSHsImportListeners() {
    }

    public void downloadImportSchema(ViewDefinitionState view, ComponentState state, String[] args) {
        this.oqshXlsxImportService.downloadImportSchema(view, QMConstants.PLUGIN_IDENTIFIER, "qSHProduct", XlsxImportService.L_XLSX);
    }

    public void processImportFile(ViewDefinitionState view, ComponentState state, String[] args) throws IOException {
        this.oqshXlsxImportService.processImportFile(view, this.oqshCellBinderRegistry.getCellBinderRegistry(), true, QMConstants.PLUGIN_IDENTIFIER, QMConstants.MODEL_QUALITY_STANDARD_H);
    }

    public void redirectToLogs(ViewDefinitionState view, ComponentState state, String[] args) {
        this.oqshXlsxImportService.redirectToLogs(view, QMConstants.MODEL_QUALITY_STANDARD_H);
    }

    public void onInputChange(ViewDefinitionState view, ComponentState state, String[] args) {
        this.oqshXlsxImportService.changeButtonsState(view, false);
    }
}

package com.fti.qm.listeners.qSImport;

import com.fti.qm.constants.QMConstants;
import com.fti.qm.imports.qshProduct.IQSHXlsxImportService;
import com.fti.qm.imports.qshProduct.QSHProductCellBinderRegistry;
import com.qcadoo.mes.basic.imports.services.XlsxImportService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class IQSHsImportListeners {

    @Autowired
    private IQSHXlsxImportService iqshXlsxImportService;
    @Autowired
    private QSHProductCellBinderRegistry iqshCellBinderRegistry;

    public IQSHsImportListeners() {
    }

    public void downloadImportSchema(ViewDefinitionState view, ComponentState state, String[] args) {
        this.iqshXlsxImportService.downloadImportSchema(view, QMConstants.PLUGIN_IDENTIFIER, "qSHProduct", XlsxImportService.L_XLSX);
    }

    public void processImportFile(ViewDefinitionState view, ComponentState state, String[] args) throws IOException {
        this.iqshXlsxImportService.processImportFile(view, this.iqshCellBinderRegistry.getCellBinderRegistry(), true, QMConstants.PLUGIN_IDENTIFIER, QMConstants.MODEL_QUALITY_STANDARD_H);
    }

    public void redirectToLogs(ViewDefinitionState view, ComponentState state, String[] args) {
        this.iqshXlsxImportService.redirectToLogs(view, QMConstants.MODEL_QUALITY_STANDARD_H);
    }

    public void onInputChange(ViewDefinitionState view, ComponentState state, String[] args) {
        this.iqshXlsxImportService.changeButtonsState(view, false);
    }
}

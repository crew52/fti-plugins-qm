package com.fti.qm.listeners.qSImport;

import com.fti.qm.imports.qshProduct.IQSHXlsxImportService;
import com.fti.qm.imports.qshProduct.QSHProductCellBinderRegistry;
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
        this.iqshXlsxImportService.downloadImportSchema(view, "qm", "qSHProduct", "xlsx");
    }

    public void processImportFile(ViewDefinitionState view, ComponentState state, String[] args) throws IOException {
        this.iqshXlsxImportService.processImportFile(view, this.iqshCellBinderRegistry.getCellBinderRegistry(), true, "qm", "qualityStandardH");
    }

    public void redirectToLogs(ViewDefinitionState view, ComponentState state, String[] args) {
        this.iqshXlsxImportService.redirectToLogs(view, "qualityStandardH");
    }

    public void onInputChange(ViewDefinitionState view, ComponentState state, String[] args) {
        this.iqshXlsxImportService.changeButtonsState(view, false);
    }
}

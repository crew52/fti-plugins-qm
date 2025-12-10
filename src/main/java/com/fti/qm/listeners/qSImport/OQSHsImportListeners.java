package com.fti.qm.listeners.qSImport;

import com.fti.qm.imports.iQSH.IQSHCellBinderRegistry;
import com.fti.qm.imports.oQSH.OQSHXlsxImportService;
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
    private IQSHCellBinderRegistry iqshCellBinderRegistry;

    public OQSHsImportListeners() {
    }

    public void downloadImportSchema(ViewDefinitionState view, ComponentState state, String[] args) {
        this.oqshXlsxImportService.downloadImportSchema(view, "qm", "iQSH", "xlsx");
    }

    public void processImportFile(ViewDefinitionState view, ComponentState state, String[] args) throws IOException {
        this.oqshXlsxImportService.processImportFile(view, this.iqshCellBinderRegistry.getCellBinderRegistry(), true, "qm", "qualityStandardH");
    }

    public void redirectToLogs(ViewDefinitionState view, ComponentState state, String[] args) {
        this.oqshXlsxImportService.redirectToLogs(view, "qualityStandardH");
    }

    public void onInputChange(ViewDefinitionState view, ComponentState state, String[] args) {
        this.oqshXlsxImportService.changeButtonsState(view, false);
    }
}

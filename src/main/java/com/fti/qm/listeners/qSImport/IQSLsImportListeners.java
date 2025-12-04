package com.fti.qm.listeners.qSImport;

import com.fti.qm.imports.iQSL.IQSLCellBinderRegistry;
import com.fti.qm.imports.iQSL.IQSLXlsxImportService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class IQSLsImportListeners {
    @Autowired
    private IQSLXlsxImportService iqslXlsxImportService;
    @Autowired
    private IQSLCellBinderRegistry iqslCellBinderRegistry;

    public IQSLsImportListeners() {
    }

    public void downloadImportSchema(ViewDefinitionState view, ComponentState state, String[] args) {
        this.iqslXlsxImportService.downloadImportSchema(view, "qm", "iQSL", "xlsx");
    }

    public void processImportFile(ViewDefinitionState view, ComponentState state, String[] args) throws IOException {
        this.iqslXlsxImportService.processImportFile(view, this.iqslCellBinderRegistry.getCellBinderRegistry(), true, "qm", "qualityStandardL");
    }

    public void redirectToLogs(ViewDefinitionState view, ComponentState state, String[] args) {
        this.iqslXlsxImportService.redirectToLogs(view, "qualityStandardL");
    }

    public void onInputChange(ViewDefinitionState view, ComponentState state, String[] args) {
        this.iqslXlsxImportService.changeButtonsState(view, false);
    }
}

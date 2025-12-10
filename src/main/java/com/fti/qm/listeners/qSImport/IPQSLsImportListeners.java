package com.fti.qm.listeners.qSImport;

import com.fti.qm.constants.QMConstants;
import com.fti.qm.imports.ipQSL.IPQSLCellBinderRegistry;
import com.fti.qm.imports.ipQSL.IPQSLXlsxImportService;
import com.qcadoo.mes.basic.imports.services.XlsxImportService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class IPQSLsImportListeners {
    @Autowired
    private IPQSLXlsxImportService ipqslXlsxImportService;
    @Autowired
    private IPQSLCellBinderRegistry ipqslCellBinderRegistry;

    public IPQSLsImportListeners() {
    }

    public void downloadImportSchema(ViewDefinitionState view, ComponentState state, String[] args) {
        this.ipqslXlsxImportService.downloadImportSchema(view, QMConstants.PLUGIN_IDENTIFIER, "qSLProduct", XlsxImportService.L_XLSX);
    }

    public void processImportFile(ViewDefinitionState view, ComponentState state, String[] args) throws IOException {
        this.ipqslXlsxImportService.processImportFile(view, this.ipqslCellBinderRegistry.getCellBinderRegistry(), true, QMConstants.PLUGIN_IDENTIFIER, QMConstants.MODEL_QUALITY_STANDARD_L);
    }

    public void redirectToLogs(ViewDefinitionState view, ComponentState state, String[] args) {
        this.ipqslXlsxImportService.redirectToLogs(view, QMConstants.MODEL_QUALITY_STANDARD_L);
    }

    public void onInputChange(ViewDefinitionState view, ComponentState state, String[] args) {
        this.ipqslXlsxImportService.changeButtonsState(view, false);
    }
}

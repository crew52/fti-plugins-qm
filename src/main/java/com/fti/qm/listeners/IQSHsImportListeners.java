package com.fti.qm.listeners;

import com.fti.qm.imports.iQSH.IQSHCellBinderRegistry;
import com.fti.qm.imports.iQSH.IQSHXlsxImportService;
import com.qcadoo.mes.basic.listeners.WorkstationsImportListeners;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriterion;
import com.qcadoo.model.api.search.SearchRestrictions;
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
    private IQSHCellBinderRegistry iqshCellBinderRegistry;

    public IQSHsImportListeners() {
    }

    public void downloadImportSchema(ViewDefinitionState view, ComponentState state, String[] args) {
        this.iqshXlsxImportService.downloadImportSchema(view, "qm", "iQSH", "xlsx");
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

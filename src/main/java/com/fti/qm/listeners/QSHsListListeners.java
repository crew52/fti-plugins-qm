package com.fti.qm.listeners;

import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import org.springframework.stereotype.Service;

@Service
public class QSHsListListeners {
    public void openIQSHsImportPage(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        StringBuilder url = new StringBuilder("../page/qm/iQSHsImport.html");

        view.openModal(url.toString());
    }

    public void openOQSHsImportPage(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        StringBuilder url = new StringBuilder("../page/qm/oQSHsImport.html");

        view.openModal(url.toString());
    }
}

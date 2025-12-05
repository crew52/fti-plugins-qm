package com.fti.qm.listeners;

import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import org.springframework.stereotype.Service;

@Service
public class QSLsListListeners {
    public void openIQSLsImportPage(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        StringBuilder url = new StringBuilder("../page/qm/iQSLsImport.html");

        view.openModal(url.toString());
    }
}

package com.fti.qm.listeners;

import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import org.springframework.stereotype.Service;

@Service
public class MeasuringEquipmentsListListeners {
    public void openMeasuringEquipmentsImportPage(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        StringBuilder url = new StringBuilder("../page/qm/measuringEquipmentsImport.html");

        view.openModal(url.toString());
    }
}

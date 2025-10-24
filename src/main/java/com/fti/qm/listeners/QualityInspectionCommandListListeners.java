package com.fti.qm.listeners;

import com.fti.qm.services.QICContextService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class QualityInspectionCommandListListeners {
    @Autowired
    private QICContextService qICContextService;

    public void confirmContext(final ViewDefinitionState viewDefinitionState, final ComponentState triggerState,
                               final String args[]) {
        qICContextService.confirmOrChangeContext(viewDefinitionState, triggerState, args);
    }
}

package com.fti.qm.hooks;

import com.fti.qm.services.QICContextService;
import com.qcadoo.view.api.ViewDefinitionState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class QICHooks {
    @Autowired
    private QICContextService qICContextService;

    public final void onBeforeRenderQICListView(final ViewDefinitionState view) {
        qICContextService.beforeRenderListView(view);
    }
}

package com.fti.qm.listeners;

import com.fti.qm.constants.qualityInspectionCommand.QICContextFields;
import com.fti.qm.services.QICContextService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.LookupComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class QualityInspectionCommandListListeners {
    @Autowired
    private QICContextService qICContextService;
    public static final String NAME = "name";
    public void confirmContext(final ViewDefinitionState viewDefinitionState, final ComponentState triggerState,
                               final String args[]) {
        qICContextService.confirmOrChangeContext(viewDefinitionState, triggerState, args);
    }

    public void resetContextDirect(final ViewDefinitionState viewDefinitionState, final ComponentState triggerState, final String[] args) {
        qICContextService.resetContext(viewDefinitionState, triggerState, args);
    }

    public void onCompanySelected(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        copyLookupNameToField(view, QICContextFields.COMPANY, QICContextFields.COMPANY_NAME);
    }

    public void onProductSelected(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        copyLookupNameToField(view, QICContextFields.PRODUCT, QICContextFields.PRODUCT_NAME);
    }

    public void onToolSelected(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        copyLookupNameToField(view, QICContextFields.TOOL, QICContextFields.TOOL_NAME);
    }

    private void copyLookupNameToField(final ViewDefinitionState view, final String lookupRef, final String fieldRef) {
        LookupComponent lookup = (LookupComponent) view.getComponentByReference(lookupRef);
        FieldComponent textField = (FieldComponent) view.getComponentByReference(fieldRef);

        Entity entity = lookup.getEntity();
        if (entity != null) {
            textField.setFieldValue(entity.getStringField(NAME));
        } else {
            textField.setFieldValue(null);
        }

        textField.requestComponentUpdateState();
    }
}

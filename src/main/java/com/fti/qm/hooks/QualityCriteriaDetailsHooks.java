package com.fti.qm.hooks;

import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.stereotype.Service;

@Service
public class QualityCriteriaDetailsHooks {
    public void beforeRender(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        // Nếu form đang edit (entity đã có ID)
        if (form.getEntityId() != null) {
            // Disable các trường
            view.getComponentByReference("number").setEnabled(false);
            view.getComponentByReference("type").setEnabled(false);
            view.getComponentByReference("unit").setEnabled(false);
        }
    }
}

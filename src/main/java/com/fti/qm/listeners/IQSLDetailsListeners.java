package com.fti.qm.listeners;

import com.fti.qm.constants.IncomingQualityStandardLFields;
import com.fti.qm.constants.QualityCriteriaFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.LookupComponent;
import org.springframework.stereotype.Service;

@Service
public class IQSLDetailsListeners {
    public final void onQualityCriteriaChange(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        LookupComponent qualityCriteriaField = (LookupComponent) view.getComponentByReference(IncomingQualityStandardLFields.QUALITY_CRITERIA);

        if (qualityCriteriaField.getFieldValue() == null) {
            return;
        }

        Entity qualityCriteria = qualityCriteriaField.getEntity();
        if (qualityCriteria == null) {
            return;
        }

        FieldComponent unitField = (FieldComponent) view.getComponentByReference(IncomingQualityStandardLFields.UNIT);

        if (unitField.getFieldValue() != null && !"".equals(unitField.getFieldValue().toString())) {
            return;
        }

        Object unitValue = qualityCriteria.getField(QualityCriteriaFields.UNIT);
        if (unitValue != null) {
            unitField.setFieldValue(unitValue);
            unitField.requestComponentUpdateState();
        }
    }

}

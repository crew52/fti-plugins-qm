package com.fti.qm.listeners.standard.base;

import com.fti.qm.constants.QualityCriteriaFields;
import com.fti.qm.utils.DecimalFieldFormatter;
import com.fti.qm.utils.DecimalFieldListenerUtils;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.LookupComponent;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

public abstract class BaseQualityStandardDetailsListener<T extends QualityStandardLineFields> {

    @Autowired protected DecimalFieldFormatter decimalFieldFormatter;
    @Autowired protected DecimalFieldListenerUtils decimalFieldListenerUtils;

    protected final T fields;

    protected BaseQualityStandardDetailsListener(T fields) {
        this.fields = fields;
    }

    public final void onQualityCriteriaChange(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        LookupComponent qcField = (LookupComponent) view.getComponentByReference(fields.QUALITY_CRITERIA());
        if (qcField.getFieldValue() == null) return;

        Entity qualityCriteria = qcField.getEntity();
        if (qualityCriteria == null) return;

        FieldComponent unitField = (FieldComponent) view.getComponentByReference(fields.UNIT());
        if (unitField.getFieldValue() != null && !"".equals(unitField.getFieldValue().toString())) return;

        Object unitValue = qualityCriteria.getField(QualityCriteriaFields.UNIT);
        if (unitValue != null) {
            unitField.setFieldValue(unitValue);
            unitField.requestComponentUpdateState();
        }
    }

    public void onUpValueChange(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        decimalFieldListenerUtils.handleDecimalInput(view, state);
    }

    public void onDownValueChange(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        decimalFieldListenerUtils.handleDecimalInput(view, state);
    }

    public void onQuantitativeValueChange(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        decimalFieldFormatter.normalizeDecimalField((FieldComponent) state, true);
        calculateUpAndDownValue(view);
    }

    public void onToleranceChange(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        decimalFieldFormatter.normalizeDecimalField((FieldComponent) state, true);
        calculateUpAndDownValue(view);
    }

    private void calculateUpAndDownValue(final ViewDefinitionState view) {
        FieldComponent quantitative = (FieldComponent) view.getComponentByReference(fields.QUANTITATIVE_VALUE());
        FieldComponent tolerance = (FieldComponent) view.getComponentByReference(fields.TOLERANCE());
        FieldComponent upValue = (FieldComponent) view.getComponentByReference(fields.UP_VALUE());
        FieldComponent downValue = (FieldComponent) view.getComponentByReference(fields.DOWN_VALUE());

        BigDecimal qVal = decimalFieldFormatter.normalizeDecimalField(quantitative, true);
        BigDecimal tVal = decimalFieldFormatter.normalizeDecimalField(tolerance, true);

        if (qVal == null || tVal == null) return;

        upValue.setFieldValue(decimalFieldFormatter.formatDecimalForLocale(qVal.add(tVal)));
        upValue.requestComponentUpdateState();

        downValue.setFieldValue(decimalFieldFormatter.formatDecimalForLocale(qVal.subtract(tVal)));
        downValue.requestComponentUpdateState();
    }
}
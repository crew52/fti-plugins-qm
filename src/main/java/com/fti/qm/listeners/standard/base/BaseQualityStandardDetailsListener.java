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

public abstract class BaseQualityStandardDetailsListener {

    @Autowired protected DecimalFieldFormatter decimalFieldFormatter;
    @Autowired protected DecimalFieldListenerUtils decimalFieldListenerUtils;

    // ✅ Các method abstract để subclass override tên field
    protected abstract String QUALITY_CRITERIA();
    protected abstract String UNIT();
    protected abstract String QUANTITATIVE_VALUE();
    protected abstract String TOLERANCE();
    protected abstract String UP_VALUE();
    protected abstract String DOWN_VALUE();

    public final void onQualityCriteriaChange(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        LookupComponent qcField = (LookupComponent) view.getComponentByReference(QUALITY_CRITERIA());
        if (qcField.getFieldValue() == null) return;

        Entity qualityCriteria = qcField.getEntity();
        if (qualityCriteria == null) return;

        FieldComponent unitField = (FieldComponent) view.getComponentByReference(UNIT());
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
        FieldComponent quantitative = (FieldComponent) view.getComponentByReference(QUANTITATIVE_VALUE());
        FieldComponent tolerance = (FieldComponent) view.getComponentByReference(TOLERANCE());
        FieldComponent upValue = (FieldComponent) view.getComponentByReference(UP_VALUE());
        FieldComponent downValue = (FieldComponent) view.getComponentByReference(DOWN_VALUE());

        BigDecimal qVal = decimalFieldFormatter.normalizeDecimalField(quantitative, true);
        BigDecimal tVal = decimalFieldFormatter.normalizeDecimalField(tolerance, true);

        if (qVal == null || tVal == null) return;

        upValue.setFieldValue(decimalFieldFormatter.formatDecimalForLocale(qVal.add(tVal)));
        upValue.requestComponentUpdateState();

        downValue.setFieldValue(decimalFieldFormatter.formatDecimalForLocale(qVal.subtract(tVal)));
        downValue.requestComponentUpdateState();
    }
}
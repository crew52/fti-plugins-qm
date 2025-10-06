    package com.fti.qm.listeners;

    import com.fti.qm.constants.InComingQualityStandardLFields;
    import com.fti.qm.constants.QualityCriteriaFields;
    import com.fti.qm.utils.DecimalFieldFormatter;
    import com.fti.qm.utils.DecimalFieldListenerUtils;
    import com.qcadoo.model.api.Entity;
    import com.qcadoo.view.api.ComponentState;
    import com.qcadoo.view.api.ViewDefinitionState;
    import com.qcadoo.view.api.components.FieldComponent;
    import com.qcadoo.view.api.components.LookupComponent;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.stereotype.Service;

    import java.math.BigDecimal;

    @Service
    public class IQSLDetailsListeners {

        @Autowired
        private DecimalFieldFormatter decimalFieldFormatter;

        @Autowired
        private DecimalFieldListenerUtils decimalFieldListenerUtils;
        /**
         * Khi thay đổi tiêu chí chất lượng, nếu field Đơn vị (unit) đang trống thì tự động lấy đơn vị từ tiêu chí chất lượng
         */
        public final void onQualityCriteriaChange(final ViewDefinitionState view, final ComponentState state, final String[] args) {
            LookupComponent qualityCriteriaField = (LookupComponent) view.getComponentByReference(InComingQualityStandardLFields.QUALITY_CRITERIA);

            if (qualityCriteriaField.getFieldValue() == null) {
                return;
            }

            Entity qualityCriteria = qualityCriteriaField.getEntity();
            if (qualityCriteria == null) {
                return;
            }

            FieldComponent unitField = (FieldComponent) view.getComponentByReference(InComingQualityStandardLFields.UNIT);

            if (unitField.getFieldValue() != null && !"".equals(unitField.getFieldValue().toString())) {
                return;
            }

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

        /**
         * Tính toán lại giá trị Up và Down khi có thay đổi ở Quantitative Value hoặc Tolerance
         * Công thức:
         * - Up Value = Quantitative Value + Tolerance
         * - Down Value = Quantitative Value - Tolerance
         */
        private void calculateUpAndDownValue(final ViewDefinitionState view) {
            FieldComponent quantitativeField = (FieldComponent) view.getComponentByReference(InComingQualityStandardLFields.QUANTITATIVE_VALUE);
            FieldComponent toleranceField = (FieldComponent) view.getComponentByReference(InComingQualityStandardLFields.TOLERANCE);
            FieldComponent upValueField = (FieldComponent) view.getComponentByReference(InComingQualityStandardLFields.UP_VALUE);
            FieldComponent downValueField = (FieldComponent) view.getComponentByReference(InComingQualityStandardLFields.DOWN_VALUE);

            BigDecimal qVal = decimalFieldFormatter.normalizeDecimalField(quantitativeField, true);
            BigDecimal tolVal = decimalFieldFormatter.normalizeDecimalField(toleranceField, true);

            if (qVal == null || tolVal == null) {
                return;
            }

            BigDecimal upVal = qVal.add(tolVal);
            BigDecimal downVal = qVal.subtract(tolVal);

            upValueField.setFieldValue(decimalFieldFormatter.formatDecimalForLocale(upVal));
            upValueField.requestComponentUpdateState();

            downValueField.setFieldValue(decimalFieldFormatter.formatDecimalForLocale(downVal));
            downValueField.requestComponentUpdateState();
        }


    }

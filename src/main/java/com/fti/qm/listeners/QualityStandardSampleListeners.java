package com.fti.qm.listeners;

import com.fti.qm.constants.QualityStandardSampleFields;
import com.fti.qm.constants.QualityStandardSampleViewFields;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class QualityStandardSampleListeners {

    public void onQuantitativeResultChange(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FieldComponent evaluationField = (FieldComponent) view.getComponentByReference(QualityStandardSampleViewFields.QUANTITATIVE_EVALUATION);
        if (evaluationField == null) return;

        BigDecimal quantitativeResult = getDecimalFieldValue(view, QualityStandardSampleViewFields.QUANTITATIVE_RESULT);
        BigDecimal upValue = getDecimalFieldValue(view, QualityStandardSampleViewFields.UP_VALUE);
        BigDecimal downValue = getDecimalFieldValue(view, QualityStandardSampleViewFields.DOWN_VALUE);

        // Tính kết quả pass/fail
        String result = calculateResult(quantitativeResult, downValue, upValue);

        // Cập nhật field
        evaluationField.setFieldValue(result);
        evaluationField.requestComponentUpdateState();
    }

    private String calculateResult(BigDecimal value, BigDecimal lower, BigDecimal upper) {
        if (value == null) return null;

        if (lower != null && upper != null) {
            return value.compareTo(lower) >= 0 && value.compareTo(upper) <= 0 ? QualityStandardSampleFields.RESULT_VALUE_PASS : QualityStandardSampleFields.RESULT_VALUE_FAIL;
        } else if (upper != null) {
            return value.compareTo(upper) <= 0 ? QualityStandardSampleFields.RESULT_VALUE_PASS : QualityStandardSampleFields.RESULT_VALUE_FAIL;
        } else if (lower != null) {
            return value.compareTo(lower) >= 0 ? QualityStandardSampleFields.RESULT_VALUE_PASS : QualityStandardSampleFields.RESULT_VALUE_FAIL;
        }
        return null; // cả hai null
    }

    private BigDecimal getDecimalFieldValue(final ViewDefinitionState view, final String reference) {
        FieldComponent field = (FieldComponent) view.getComponentByReference(reference);
        if (field == null || field.getFieldValue() == null) return null;
        try {
            return new BigDecimal(field.getFieldValue().toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}

package com.fti.qm.hooks.outgoingQualityStandard;

import com.fti.qm.constants.QualityCriteriaFields;
import com.fti.qm.constants.outgoingQualityStandard.OutgoingQualityStandardLFields;
import org.springframework.stereotype.Service;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.DataDefinition;

@Service
public class OutgoingQualityStandardLHooks {


    /**
     * Hook xử lý khi lưu form.
     * - Lấy checkbox qualitativeCheckbox → lưu vào enum qualitativeValue.
     * - Nếu chưa có unit → tự động lấy từ qualityCriteria.
     */
    public void onSave(final DataDefinition dataDefinition, final Entity entity) {
        convertCheckboxToEnum(entity);
        setUnitFromQualityCriteria(entity);
    }

    /**
     * Hook xử lý khi mở view (edit mode).
     * - Lấy enum qualitativeValue → đặt lại checkbox.
     */
    public void onView(final DataDefinition dataDefinition, final Entity entity) {
        Object enumValue = entity.getField(OutgoingQualityStandardLFields.QUALITATIVE_VALUE);

        // Chỉ tick checkbox nếu enum = "01pass"
        entity.setField(OutgoingQualityStandardLFields.QUALITATIVE_CHECKBOX_VALUE, OutgoingQualityStandardLFields.QUALITATIVE_VALUE_PASS.equals(enumValue));
    }


    /**
     * Lấy checkbox qualitativeCheckbox (false/true) → gán vào qualitativeValue.
     */
    private void convertCheckboxToEnum(final Entity entity) {
        Object checkboxValue = entity.getField(OutgoingQualityStandardLFields.QUALITATIVE_CHECKBOX_VALUE);

        if (checkboxValue instanceof Boolean) {
            boolean checked = (Boolean) checkboxValue;
            entity.setField(OutgoingQualityStandardLFields.QUALITATIVE_VALUE,
                    checked ? OutgoingQualityStandardLFields.QUALITATIVE_VALUE_PASS : null);
        }
    }

    /**
     * Nếu chưa có unit → lấy từ qualityCriteria.
     */
    private void setUnitFromQualityCriteria(final Entity entity) {
        if (entity.getField(OutgoingQualityStandardLFields.UNIT) == null) {
            Entity qualityCriteria = entity.getBelongsToField(OutgoingQualityStandardLFields.QUALITY_CRITERIA);

            if (qualityCriteria != null && qualityCriteria.getField(QualityCriteriaFields.UNIT) != null) {
                entity.setField(OutgoingQualityStandardLFields.UNIT,
                        qualityCriteria.getField(QualityCriteriaFields.UNIT));
            }
        }
    }
}


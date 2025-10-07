package com.fti.qm.hooks;

import com.fti.qm.constants.IncomingQualityStandardLFields;
import com.fti.qm.constants.QualityCriteriaFields;
import org.springframework.stereotype.Service;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.DataDefinition;

@Service
public class IncomingQualityStandardLHooks {
    public void onSave(final DataDefinition dataDefinition, final Entity entity) {
        convertCheckboxToEnum(entity);
        setUnitFromQualityCriteria(entity);
    }

    public void onView(final DataDefinition dataDefinition, final Entity entity) {
        Object enumValue = entity.getField(IncomingQualityStandardLFields.QUALITATIVE_VALUE);
        entity.setField(IncomingQualityStandardLFields.QUALITATIVE_CHECKBOX_VALUE, IncomingQualityStandardLFields.QUALITATIVE_VALUE_PASS.equals(enumValue));
    }

    private void convertCheckboxToEnum(final Entity entity) {
        Object checkboxValue = entity.getField(IncomingQualityStandardLFields.QUALITATIVE_CHECKBOX_VALUE);

        if (checkboxValue instanceof Boolean) {
            boolean checked = (Boolean) checkboxValue;
            entity.setField(IncomingQualityStandardLFields.QUALITATIVE_VALUE,
                    checked ? IncomingQualityStandardLFields.QUALITATIVE_VALUE_PASS : null);
        }
    }
    private void setUnitFromQualityCriteria(final Entity entity) {
        if (entity.getField(IncomingQualityStandardLFields.UNIT) == null) {
            Entity qualityCriteria = entity.getBelongsToField(IncomingQualityStandardLFields.QUALITY_CRITERIA);

            if (qualityCriteria != null && qualityCriteria.getField(QualityCriteriaFields.UNIT) != null) {
                entity.setField(IncomingQualityStandardLFields.UNIT,
                        qualityCriteria.getField(QualityCriteriaFields.UNIT));
            }
        }
    }
}

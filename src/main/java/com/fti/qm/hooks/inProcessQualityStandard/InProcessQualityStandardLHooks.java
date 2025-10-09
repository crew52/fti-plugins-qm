package com.fti.qm.hooks.inProcessQualityStandard;

import com.fti.qm.constants.QualityCriteriaFields;
import com.fti.qm.constants.inProcessQualityStandard.InProcessQualityStandardLFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import org.springframework.stereotype.Service;

@Service
public class InProcessQualityStandardLHooks {

    public void onSave(final DataDefinition dataDefinition, final Entity entity) {
        convertCheckboxToEnum(entity);
        setUnitFromQualityCriteria(entity);
    }

    public void onView(final DataDefinition dataDefinition, final Entity entity) {
        Object enumValue = entity.getField(InProcessQualityStandardLFields.QUALITATIVE_VALUE);

        entity.setField(InProcessQualityStandardLFields.QUALITATIVE_CHECKBOX_VALUE, InProcessQualityStandardLFields.QUALITATIVE_VALUE_PASS.equals(enumValue));
    }

    private void convertCheckboxToEnum(final Entity entity) {
        Object checkboxValue = entity.getField(InProcessQualityStandardLFields.QUALITATIVE_CHECKBOX_VALUE);

        if (checkboxValue instanceof Boolean) {
            boolean checked = (Boolean) checkboxValue;
            entity.setField(InProcessQualityStandardLFields.QUALITATIVE_VALUE,
                    checked ? InProcessQualityStandardLFields.QUALITATIVE_VALUE_PASS : null);
        }
    }

    private void setUnitFromQualityCriteria(final Entity entity) {
        if (entity.getField(InProcessQualityStandardLFields.UNIT) == null) {
            Entity qualityCriteria = entity.getBelongsToField(InProcessQualityStandardLFields.QUALITY_CRITERIA);

            if (qualityCriteria != null && qualityCriteria.getField(QualityCriteriaFields.UNIT) != null) {
                entity.setField(InProcessQualityStandardLFields.UNIT,
                        qualityCriteria.getField(QualityCriteriaFields.UNIT));
            }
        }
    }
}


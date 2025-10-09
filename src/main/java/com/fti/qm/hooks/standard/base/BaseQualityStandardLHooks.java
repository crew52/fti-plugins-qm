package com.fti.qm.hooks.standard.base;

import com.fti.qm.constants.QualityCriteriaFields;
import com.qcadoo.model.api.Entity;

public abstract class BaseQualityStandardLHooks {

    protected abstract String getQualitativeCheckboxField();
    protected abstract String getQualitativeValueField();
    protected abstract String getQualitativePassConstant();
    protected abstract String getUnitField();
    protected abstract String getQualityCriteriaField();

    public void onSave(final Entity entity) {
        convertCheckboxToEnum(entity);
        setUnitFromQualityCriteria(entity);
    }

    public void onView(final Entity entity) {
        Object enumValue = entity.getField(getQualitativeValueField());
        entity.setField(getQualitativeCheckboxField(), getQualitativePassConstant().equals(enumValue));
    }

    private void convertCheckboxToEnum(final Entity entity) {
        Object checkboxValue = entity.getField(getQualitativeCheckboxField());

        if (checkboxValue instanceof Boolean) {
            Boolean checked = (Boolean) checkboxValue;
            entity.setField(getQualitativeValueField(), checked ? getQualitativePassConstant() : null);
        }
    }

    private void setUnitFromQualityCriteria(final Entity entity) {
        if (entity.getField(getUnitField()) == null) {
            Entity qualityCriteria = entity.getBelongsToField(getQualityCriteriaField());

            if (qualityCriteria != null && qualityCriteria.getField(QualityCriteriaFields.UNIT) != null) {
                entity.setField(getUnitField(), qualityCriteria.getField(QualityCriteriaFields.UNIT));
            }
        }
    }
}

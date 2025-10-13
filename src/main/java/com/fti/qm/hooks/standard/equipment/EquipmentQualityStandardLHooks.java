package com.fti.qm.hooks.standard.equipment;

import com.fti.qm.constants.equipmentQualityStandardH.EquipmentQualityStandardLFields;
import com.fti.qm.hooks.standard.base.BaseQualityStandardLHooks;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import org.springframework.stereotype.Service;

@Service
public class EquipmentQualityStandardLHooks extends BaseQualityStandardLHooks {
    @Override protected String getQualitativeCheckboxField() { return EquipmentQualityStandardLFields.QUALITATIVE_CHECKBOX_VALUE; }
    @Override protected String getQualitativeValueField()     { return EquipmentQualityStandardLFields.QUALITATIVE_VALUE; }
    @Override protected String getQualitativePassConstant()   { return EquipmentQualityStandardLFields.QUALITATIVE_VALUE_PASS; }
    @Override protected String getUnitField()                 { return EquipmentQualityStandardLFields.UNIT; }
    @Override protected String getQualityCriteriaField()      { return EquipmentQualityStandardLFields.QUALITY_CRITERIA; }

    public void onSave(final DataDefinition dd, final Entity entity) {
        super.onSave(entity);
    }

    public void onView(final DataDefinition dd, final Entity entity) {
        super.onView(entity);
    }
}

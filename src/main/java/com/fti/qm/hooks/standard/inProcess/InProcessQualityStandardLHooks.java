package com.fti.qm.hooks.standard.inProcess;

import com.fti.qm.constants.inProcessQualityStandard.InProcessQualityStandardLFields;
import com.fti.qm.hooks.standard.base.BaseQualityStandardLHooks;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import org.springframework.stereotype.Service;

@Service
public class InProcessQualityStandardLHooks extends BaseQualityStandardLHooks {

    @Override protected String getQualitativeCheckboxField() { return InProcessQualityStandardLFields.QUALITATIVE_CHECKBOX_VALUE; }
    @Override protected String getQualitativeValueField()     { return InProcessQualityStandardLFields.QUALITATIVE_VALUE; }
    @Override protected String getQualitativePassConstant()   { return InProcessQualityStandardLFields.QUALITATIVE_VALUE_PASS; }
    @Override protected String getUnitField()                 { return InProcessQualityStandardLFields.UNIT; }
    @Override protected String getQualityCriteriaField()      { return InProcessQualityStandardLFields.QUALITY_CRITERIA; }

    public void onSave(final DataDefinition dd, final Entity entity) {
        super.onSave(entity);
    }

    public void onView(final DataDefinition dd, final Entity entity) {
        super.onView(entity);
    }
}


package com.fti.qm.hooks.standard.incoming;

import com.fti.qm.constants.IncomingQualityStandardLFields;
import com.fti.qm.hooks.standard.base.BaseQualityStandardLHooks;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import org.springframework.stereotype.Service;

@Service
public class IncomingQualityStandardLHooks extends BaseQualityStandardLHooks {

    @Override protected String getQualitativeCheckboxField() { return IncomingQualityStandardLFields.QUALITATIVE_CHECKBOX_VALUE; }
    @Override protected String getQualitativeValueField()     { return IncomingQualityStandardLFields.QUALITATIVE_VALUE; }
    @Override protected String getQualitativePassConstant()   { return IncomingQualityStandardLFields.QUALITATIVE_VALUE_PASS; }
    @Override protected String getUnitField()                 { return IncomingQualityStandardLFields.UNIT; }
    @Override protected String getQualityCriteriaField()      { return IncomingQualityStandardLFields.QUALITY_CRITERIA; }

    public void onSave(final DataDefinition dd, final Entity entity) {
        super.onSave(entity);
    }

    public void onView(final DataDefinition dd, final Entity entity) {
        super.onView(entity);
    }
}

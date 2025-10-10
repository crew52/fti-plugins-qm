package com.fti.qm.hooks.standard.outgoing;

import com.fti.qm.constants.outgoingQualityStandard.OutgoingQualityStandardLFields;
import com.fti.qm.hooks.standard.base.BaseQualityStandardLHooks;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import org.springframework.stereotype.Service;

@Service
public class OutgoingQualityStandardLHooks extends BaseQualityStandardLHooks {

    @Override protected String getQualitativeCheckboxField() { return OutgoingQualityStandardLFields.QUALITATIVE_CHECKBOX_VALUE; }
    @Override protected String getQualitativeValueField()     { return OutgoingQualityStandardLFields.QUALITATIVE_VALUE; }
    @Override protected String getQualitativePassConstant()   { return OutgoingQualityStandardLFields.QUALITATIVE_VALUE_PASS; }
    @Override protected String getUnitField()                 { return OutgoingQualityStandardLFields.UNIT; }
    @Override protected String getQualityCriteriaField()      { return OutgoingQualityStandardLFields.QUALITY_CRITERIA; }

    public void onSave(final DataDefinition dd, final Entity entity) {
        super.onSave(entity);
    }

    public void onView(final DataDefinition dd, final Entity entity) {
        super.onView(entity);
    }
}


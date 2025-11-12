package com.fti.qm.hooks.standard;

import com.fti.qm.hooks.standard.base.BaseQualityStandardLHooks;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import org.springframework.stereotype.Service;

@Service
public class QualityStandardLHooks extends BaseQualityStandardLHooks {
    @Override protected String getQualitativeCheckboxField() { return "qualitativeCheckbox"; }
    @Override protected String getQualitativeValueField()     { return "qualitativeValue"; }
    @Override protected String getQualitativePassConstant()   { return "01pass"; }
    @Override protected String getUnitField()                 { return "unit"; }
    @Override protected String getQualityCriteriaField()      { return "qualityCriteria"; }

    public void onSave(final DataDefinition dd, final Entity entity) {
        super.onSave(entity);
    }

    public void onView(final DataDefinition dd, final Entity entity) {
        super.onView(entity);
    }
}

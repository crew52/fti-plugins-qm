package com.fti.qm.hooks.standard;

import com.fti.qm.constants.GlobalFields;
import com.fti.qm.constants.QMConstants;
import com.fti.qm.constants.QSLFields;
import com.fti.qm.helpers.SoftDeleteHelper;
import com.fti.qm.hooks.standard.base.BaseQualityStandardLHooks;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class QualityStandardLHooks extends BaseQualityStandardLHooks {

    @Autowired
    private SoftDeleteHelper softDeleteHelper;

    @Override protected String getQualitativeCheckboxField() { return QSLFields.QUALITATIVE_CHECKBOX; }
    @Override protected String getQualitativeValueField()     { return QSLFields.QUALITATIVE_VALUE; }
    @Override protected String getQualitativePassConstant()   { return QSLFields.QualitativeValue.PASS; }
    @Override protected String getUnitField()                 { return QSLFields.UNIT; }
    @Override protected String getQualityCriteriaField()      { return QSLFields.QUALITY_CRITERIA; }

    public void onSave(final DataDefinition dd, final Entity entity) {
        super.onSave(entity);
    }

    public void onView(final DataDefinition dd, final Entity entity) {
        super.onView(entity);
    }

    public void onUpdate(final DataDefinition dataDefinition, final Entity qualityStandardL) {
        Boolean deleted = qualityStandardL.getBooleanField(GlobalFields.DELETED);
        if (deleted == null || !deleted) {
            return;
        }

        // Xóa mềm các bản ghi con
        String joinField = QMConstants.MODEL_QUALITY_STANDARD_L;
        softDeleteHelper.softDeleteRelatedLines(QMConstants.MODEL_QUALITY_STANDARD_SAMPLE, joinField, qualityStandardL);
    }
}

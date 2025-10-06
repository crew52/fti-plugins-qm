package com.fti.qm.hooks;

import com.fti.qm.constants.IncomingQualityStandardLFields;
import com.fti.qm.constants.QualityCriteriaFields;
import org.springframework.stereotype.Service;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.DataDefinition;

@Service
public class IncomingQualityStandardLHooks {

    public void onSave(DataDefinition dataDefinition, Entity entity) {
        if (entity.getField(IncomingQualityStandardLFields.UNIT) == null) {
            Entity qualityCriteria = entity.getBelongsToField(IncomingQualityStandardLFields.QUALITY_CRITERIA);

            if (qualityCriteria != null && qualityCriteria.getField(QualityCriteriaFields.UNIT) != null) {
                entity.setField(IncomingQualityStandardLFields.UNIT, qualityCriteria.getField(QualityCriteriaFields.UNIT));
            }
        }
    }
}

package com.fti.qm.hooks;

import com.fti.qm.constants.IncomingQualityStandardL;
import com.fti.qm.constants.QualityCriteriaFields;
import org.springframework.stereotype.Service;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.DataDefinition;

@Service
public class IncomingQualityStandardLHooks {

    public void onSave(DataDefinition dataDefinition, Entity entity) {
        if (entity.getField(IncomingQualityStandardL.UNIT) == null) {
            Entity qualityCriteria = entity.getBelongsToField(IncomingQualityStandardL.QUALITY_CRITERIA);

            if (qualityCriteria != null && qualityCriteria.getField(QualityCriteriaFields.UNIT) != null) {
                entity.setField(IncomingQualityStandardL.UNIT, qualityCriteria.getField(QualityCriteriaFields.UNIT));
            }
        }
    }
}

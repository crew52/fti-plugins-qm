package com.fti.qm.hooks.outgoingQualityStandard;

import com.fti.qm.constants.OutgoingQualityStandardLFields;
import com.fti.qm.constants.QualityCriteriaFields;
import org.springframework.stereotype.Service;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.DataDefinition;

@Service
public class OutgoingQualityStandardLHooks {

    public void onSave(DataDefinition dataDefinition, Entity entity) {
        if (entity.getField(OutgoingQualityStandardLFields.UNIT) == null) {
            Entity qualityCriteria = entity.getBelongsToField(OutgoingQualityStandardLFields.QUALITY_CRITERIA);

            if (qualityCriteria != null && qualityCriteria.getField(QualityCriteriaFields.UNIT) != null) {
                entity.setField(OutgoingQualityStandardLFields.UNIT, qualityCriteria.getField(QualityCriteriaFields.UNIT));
            }
        }
    }
}


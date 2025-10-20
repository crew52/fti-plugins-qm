package com.fti.qm.hooks;

import com.fti.qm.constants.GlobalFields;
import com.fti.qm.constants.QMConstants;
import com.fti.qm.helpers.SoftDeleteHelper;
import com.qcadoo.model.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class QualityCriteriaHooks {

    @Autowired
    private SoftDeleteHelper softDeleteHelper;

    public void onUpdate(final DataDefinition dataDefinition, final Entity qualityCriteria) {
        Boolean deleted = qualityCriteria.getBooleanField(GlobalFields.DELETED);
        if (deleted == null || !deleted) {
            return;
        }

        // Xóa mềm các bản ghi con
        String joinField = QMConstants.MODEL_QUALITY_CRITERIA;
        softDeleteHelper.softDeleteRelatedLines(QMConstants.MODEL_INCOMING_QUALITY_STANDARD_L, joinField, qualityCriteria);
        softDeleteHelper.softDeleteRelatedLines(QMConstants.MODEL_OUTGOING_QUALITY_STANDARD_L, joinField, qualityCriteria);
        softDeleteHelper.softDeleteRelatedLines(QMConstants.MODEL_IN_PROCESS_QUALITY_STANDARD_L, joinField, qualityCriteria);
        softDeleteHelper.softDeleteRelatedLines(QMConstants.MODEL_EQUIPMENT_QUALITY_STANDARD_L, joinField, qualityCriteria);
    }
}
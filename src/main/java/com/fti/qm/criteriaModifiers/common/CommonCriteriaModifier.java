package com.fti.qm.criteriaModifiers.common;

import com.fti.qm.constants.GlobalFields;
import com.fti.qm.constants.MeasuringEquipmentFields;
import com.fti.qm.constants.QMConstants;
import com.fti.qm.constants.QualityCriteriaFields;
import com.fti.qm.helpers.EntityHelper;
import com.qcadoo.model.api.search.*;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public final class CommonCriteriaModifier {
    @Autowired
    private EntityHelper entityHelper;
    public void addDeletedFalseCondition(final SearchCriteriaBuilder scb, final FilterValueHolder filterValueHolder) {
        scb.add(SearchRestrictions.eq(GlobalFields.DELETED, false));
    }
    public void filterOnlyNonDeletedRelations(final SearchCriteriaBuilder scb, final FilterValueHolder filterValueHolder) {

        List<Long> qcIds = entityHelper.getNonDeletedIds(QMConstants.PLUGIN_IDENTIFIER, QMConstants.MODEL_QUALITY_CRITERIA);
        List<Long> meIds = entityHelper.getNonDeletedIds(QMConstants.PLUGIN_IDENTIFIER, QMConstants.MODEL_MEASURING_EQUIPMENT);

        if (qcIds.isEmpty() || meIds.isEmpty()) {
            scb.add(SearchRestrictions.idEq(-1L));
            return;
        }

        scb.add(SearchRestrictions.in(QualityCriteriaFields.QUALITY_CRITERIA_ID, qcIds))
                .add(SearchRestrictions.in(MeasuringEquipmentFields.MEASURING_EQUIPMENT_ID, meIds));
    }

}

package com.fti.qm.criteriaModifiers.common;

import com.fti.qm.constants.QSHFields;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;
import org.springframework.stereotype.Component;

@Component
public final class QSTypeCriteriaModifier {

    public void addIncomingTypeCondition(final SearchCriteriaBuilder scb, final FilterValueHolder filterValueHolder) {
        scb.add(SearchRestrictions.eq(QSHFields.TYPE, QSHFields.Type.INCOMING));
    }

    public void addInprocessTypeCondition(final SearchCriteriaBuilder scb, final FilterValueHolder filterValueHolder) {
        scb.add(SearchRestrictions.eq(QSHFields.TYPE, QSHFields.Type.INPROCESS));
    }

    public void addOutgoingTypeCondition(final SearchCriteriaBuilder scb, final FilterValueHolder filterValueHolder) {
        scb.add(SearchRestrictions.eq(QSHFields.TYPE, QSHFields.Type.OUTGOING));
    }

    public void addEquipmentTypeCondition(final SearchCriteriaBuilder scb, final FilterValueHolder filterValueHolder) {
        scb.add(SearchRestrictions.eq(QSHFields.TYPE, QSHFields.Type.EQUIPMENT));
    }
}

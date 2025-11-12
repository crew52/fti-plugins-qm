package com.fti.qm.criteriaModifiers.common;

import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;
import org.springframework.stereotype.Component;

@Component
public final class QSTypeCriteriaModifier {

    public void addIncomingTypeCondition(final SearchCriteriaBuilder scb, final FilterValueHolder filterValueHolder) {
        scb.add(SearchRestrictions.eq("type", "01incoming"));
    }

    public void addInprocessTypeCondition(final SearchCriteriaBuilder scb, final FilterValueHolder filterValueHolder) {
        scb.add(SearchRestrictions.eq("type", "02inprocess"));
    }

    public void addOutgoingTypeCondition(final SearchCriteriaBuilder scb, final FilterValueHolder filterValueHolder) {
        scb.add(SearchRestrictions.eq("type", "03outgoing"));
    }

    public void addEquipmentTypeCondition(final SearchCriteriaBuilder scb, final FilterValueHolder filterValueHolder) {
        scb.add(SearchRestrictions.eq("type", "04equipment"));
    }
}

package com.fti.qm.criteriaModifiers.common;

import com.fti.qm.constants.GlobalFields;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;
import org.springframework.stereotype.Component;

@Component
public final class CommonCriteriaModifier {
    public void addDeletedFalseCondition(final SearchCriteriaBuilder scb, final FilterValueHolder filterValueHolder) {
        scb.add(SearchRestrictions.eq(GlobalFields.DELETED, false));
    }
}

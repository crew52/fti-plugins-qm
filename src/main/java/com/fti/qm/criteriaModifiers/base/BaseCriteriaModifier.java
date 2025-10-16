package com.fti.qm.criteriaModifiers.base;

import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;

public abstract class BaseCriteriaModifier {
    public void addDeletedFalseCondition(final SearchCriteriaBuilder scb, final FilterValueHolder filterValueHolder) {
        scb.add(SearchRestrictions.eq("deleted", false));
    }
}
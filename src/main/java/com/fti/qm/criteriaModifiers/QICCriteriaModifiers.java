package com.fti.qm.criteriaModifiers;

import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;
import org.springframework.stereotype.Service;

@Service
public class QICCriteriaModifiers {

    public void warehouseLocationModifier(final SearchCriteriaBuilder scb,
                                          final FilterValueHolder filterValueHolder) {

        applyExcludeLocationFilter(scb, filterValueHolder);
    }

    public void ngLocationModifier(final SearchCriteriaBuilder scb,
                                   final FilterValueHolder filterValueHolder) {

        applyExcludeLocationFilter(scb, filterValueHolder);
    }

    private void applyExcludeLocationFilter(final SearchCriteriaBuilder scb,
                                            final FilterValueHolder filterValueHolder) {

        if (filterValueHolder.has("excludedLocationId")) {
            Long excludedLocationId = filterValueHolder.getLong("excludedLocationId");

            if (excludedLocationId != null) {
                scb.add(SearchRestrictions.ne("id", excludedLocationId));
            }
        }
    }
}
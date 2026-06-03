package com.fti.qm.criteriaModifiers;

import com.fti.qm.constants.qualityInspectionCommand.QICFields;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;
import org.springframework.stereotype.Service;

/**
 * Criteria modifier cho các lookup Location trên màn hình QIC.
 *
 * <p>Quy tắc lọc:
 * <ul>
 *     <li>ACCEPT / REJECT: loại trừ location hiện tại của QIC.</li>
 *     <li>PARTIAL: hiển thị toàn bộ location, bao gồm location của QIC.</li>
 * </ul>
 * </p>
 */
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

    /**
     * Áp dụng điều kiện loại trừ location của QIC khỏi danh sách lookup.
     *
     * <p>Không áp dụng filter khi quality decision là PARTIAL.</p>
     *
     * @param scb search criteria builder
     * @param filterValueHolder filter values được truyền từ view
     */
    private void applyExcludeLocationFilter(final SearchCriteriaBuilder scb,
                                            final FilterValueHolder filterValueHolder) {

        String qualityDecision =
                filterValueHolder.getString("qualityDecision");

        if (QICFields.QualityDecision.PARTIAL.equals(qualityDecision)) {
            return;
        }

        if (filterValueHolder.has("excludedLocationId")) {

            Long excludedLocationId =
                    filterValueHolder.getLong("excludedLocationId");

            scb.add(
                    SearchRestrictions.ne("id", excludedLocationId)
            );
        }
    }
}
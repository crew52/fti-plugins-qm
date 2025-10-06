package com.fti.qm.criteriaModifiers;

import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;
import org.springframework.stereotype.Service;
@Service("qmProductCriteriaModifiers")
public class ProductCriteriaModifiers {
    private static final String PRODUCT_FIELD_ACTIVE = "active";
    private static final String PRODUCT_FIELD_GLOBAL_TYPE_OF_MATERIAL = "globalTypeOfMaterial";
    private static final String VALUE_COMPONENT = "01component";
    private static final String VALUE_INTERMEDIATE = "02intermediate";
    private static final String VALUE_FINAL_PRODUCT = "03finalProduct";

    public void filterForQualityStandard(final SearchCriteriaBuilder scb, final FilterValueHolder filterValueHolder) {
        scb.add(SearchRestrictions.eq(PRODUCT_FIELD_ACTIVE, true));

        scb.add(SearchRestrictions.or(
                SearchRestrictions.eq(PRODUCT_FIELD_GLOBAL_TYPE_OF_MATERIAL, VALUE_COMPONENT),
                SearchRestrictions.eq(PRODUCT_FIELD_GLOBAL_TYPE_OF_MATERIAL, VALUE_INTERMEDIATE)
        ));
    }


    // Dành cho tiêu chuẩn chất lượng đầu ra
    public void filterForOutgoingQualityStandard(final SearchCriteriaBuilder scb, final FilterValueHolder filterValueHolder) {
        scb.add(SearchRestrictions.eq(PRODUCT_FIELD_ACTIVE, true));

        scb.add(SearchRestrictions.eq(PRODUCT_FIELD_GLOBAL_TYPE_OF_MATERIAL, VALUE_FINAL_PRODUCT));
    }
}

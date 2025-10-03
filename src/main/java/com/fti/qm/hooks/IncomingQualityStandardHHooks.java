package com.fti.qm.hooks;

import com.fti.qm.constants.IncomingQualityStandardHFields;
import com.qcadoo.model.api.*;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class IncomingQualityStandardHHooks {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void onSave(final DataDefinition dataDefinition, final Entity incomingStandardH) {
        if (!incomingStandardH.isValid()) {
            return;
        }

        final Long productId = incomingStandardH.getBelongsToField(IncomingQualityStandardHFields.PRODUCT).getId();
        final Long currentId = incomingStandardH.getId();

        SearchCriteriaBuilder scb = dataDefinition.find()
                .add(SearchRestrictions.eq(IncomingQualityStandardHFields.PRODUCT_ID, productId))
                .add(SearchRestrictions.eq(IncomingQualityStandardHFields.ACTIVE, true));

        if (currentId != null) {
            scb.add(SearchRestrictions.ne("id", currentId));
        }

        Entity exists = scb.setMaxResults(1).uniqueResult();

        if (exists != null) {
            incomingStandardH.addError(dataDefinition.getField(IncomingQualityStandardHFields.PRODUCT),
                    "qm.message.error.productAlreadyExistsWithActiveStandard");
        }
    }
}
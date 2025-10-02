package com.fti.qm.hooks;

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

        final Long productId = incomingStandardH.getBelongsToField("product").getId();
        final Long currentId = incomingStandardH.getId();

        SearchCriteriaBuilder scb = dataDefinition.find()
                .add(SearchRestrictions.eq("product.id", productId))
                .add(SearchRestrictions.eq("active", true));

        if (currentId != null) {
            scb.add(SearchRestrictions.ne("id", currentId));
        }

        Entity exists = scb.setMaxResults(1).uniqueResult();

        if (exists != null) {
            incomingStandardH.addError(dataDefinition.getField("product"),
                    "qcadooView.message.productAlreadyExistsWithActiveStandard");
        }
    }
}
package com.fti.qm.hooks;

import com.fti.qm.constants.IncomingQualityStandardHFields;
import com.qcadoo.model.api.*;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

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

    public void setStatusText(final ViewDefinitionState view) {
        GridComponent grid = (GridComponent) view.getComponentByReference(QcadooViewConstants.L_GRID);

        if (grid == null) {
            return;
        }

        List<Entity> entities = grid.getEntities();

        for (Entity entity : entities) {
            List<Entity> lines = entity.getHasManyField(IncomingQualityStandardHFields.INCOMING_QUALITY_STANDARD_LS);

            String status = (lines == null || lines.isEmpty())
                    ? IncomingQualityStandardHFields.STATUS_NO_STANDARD
                    : IncomingQualityStandardHFields.STATUS_HAS_STANDARD;

            entity.setField(IncomingQualityStandardHFields.STATUS_TEXT, status);
        }

        grid.setEntities(entities);
    }
}
package com.fti.qm.hooks.outgoingQualityStandard;

import com.fti.qm.constants.outgoingQualityStandard.OutgoingQualityStandardHFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class OutgoingQualityStandardHHooks {
    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void onSave(final DataDefinition dataDefinition, final Entity outgoingStandardH) {
        if (!outgoingStandardH.isValid()) {
            return;
        }

        final Long productId = outgoingStandardH.getBelongsToField(OutgoingQualityStandardHFields.PRODUCT).getId();
        final Long currentId = outgoingStandardH.getId();

        SearchCriteriaBuilder scb = dataDefinition.find()
                .add(SearchRestrictions.eq(OutgoingQualityStandardHFields.PRODUCT_ID, productId))
                .add(SearchRestrictions.eq(OutgoingQualityStandardHFields.ACTIVE, true));

        if (currentId != null) {
            scb.add(SearchRestrictions.ne("id", currentId));
        }

        Entity exists = scb.setMaxResults(1).uniqueResult();

        if (exists != null) {
            outgoingStandardH.addError(dataDefinition.getField(OutgoingQualityStandardHFields.PRODUCT),
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
            List<Entity> lines = entity.getHasManyField(OutgoingQualityStandardHFields.OUTGOING_QUALITY_STANDARD_LS);

            String status = (lines == null || lines.isEmpty())
                    ? OutgoingQualityStandardHFields.STATUS_NO_STANDARD
                    : OutgoingQualityStandardHFields.STATUS_HAS_STANDARD;

            entity.setField(OutgoingQualityStandardHFields.STATUS_TEXT, status);
        }

        grid.setEntities(entities);
    }
}


package com.fti.qm.hooks.inProcessQualityStandard;

import com.fti.qm.constants.inProcessQualityStandard.InProcessQualityStandardHFields;
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
public class InProcessQualityStandardHHooks {
    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void onSave(final DataDefinition dataDefinition, final Entity inProcessStandardH) {
        if (!inProcessStandardH.isValid()) {
            return;
        }

        final Long productId = inProcessStandardH.getBelongsToField(InProcessQualityStandardHFields.PRODUCT).getId();
        final Long currentId = inProcessStandardH.getId();

        SearchCriteriaBuilder scb = dataDefinition.find()
                .add(SearchRestrictions.eq(InProcessQualityStandardHFields.PRODUCT_ID, productId))
                .add(SearchRestrictions.eq(InProcessQualityStandardHFields.ACTIVE, true));

        if (currentId != null) {
            scb.add(SearchRestrictions.ne("id", currentId));
        }

        Entity exists = scb.setMaxResults(1).uniqueResult();

        if (exists != null) {
            inProcessStandardH.addError(dataDefinition.getField(InProcessQualityStandardHFields.PRODUCT),
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
            List<Entity> lines = entity.getHasManyField(InProcessQualityStandardHFields.IN_PROCESS_QUALITY_STANDARD_LS);

            String status = (lines == null || lines.isEmpty())
                    ? InProcessQualityStandardHFields.STATUS_NO_STANDARD
                    : InProcessQualityStandardHFields.STATUS_HAS_STANDARD;

            entity.setField(InProcessQualityStandardHFields.STATUS_TEXT, status);
        }

        grid.setEntities(entities);
    }
}

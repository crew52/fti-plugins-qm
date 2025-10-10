package com.fti.qm.hooks.standard.base;

import com.qcadoo.model.api.*;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.constants.QcadooViewConstants;

import java.util.List;

public abstract class BaseQualityStandardHooks {

    private final DataDefinitionService dataDefinitionService;

    protected BaseQualityStandardHooks(DataDefinitionService dataDefinitionService) {
        this.dataDefinitionService = dataDefinitionService;
    }

    public void handleOnSave(final DataDefinition dataDefinition, final Entity entity,
                             String productField, String productIdField, String activeField,
                             String errorMessageKey) {
        if (!entity.isValid()) {
            return;
        }

        Long productId = entity.getBelongsToField(productField).getId();
        Long currentId = entity.getId();

        SearchCriteriaBuilder scb = dataDefinition.find()
                .add(SearchRestrictions.eq(productIdField, productId))
                .add(SearchRestrictions.eq(activeField, true));

        if (currentId != null) {
            scb.add(SearchRestrictions.ne("id", currentId));
        }

        Entity exists = scb.setMaxResults(1).uniqueResult();

        if (exists != null) {
            entity.addError(dataDefinition.getField(productField), errorMessageKey);
        }
    }

    public void handleSetStatusText(final ViewDefinitionState view,
                                    String linesField, String statusTextField,
                                    String statusNoStandard, String statusHasStandard) {
        GridComponent grid = (GridComponent) view.getComponentByReference(QcadooViewConstants.L_GRID);

        if (grid == null) {
            return;
        }

        List<Entity> entities = grid.getEntities();

        for (Entity entity : entities) {
            List<Entity> lines = entity.getHasManyField(linesField);

            String status = (lines == null || lines.isEmpty()) ? statusNoStandard : statusHasStandard;
            entity.setField(statusTextField, status);
        }

        grid.setEntities(entities);
    }

}

package com.fti.qm.hooks.standard.base;

import com.fti.qm.constants.GlobalFields;
import com.qcadoo.model.api.*;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.constants.QcadooViewConstants;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseQualityStandardHooks {

    private final DataDefinitionService dataDefinitionService;

    protected BaseQualityStandardHooks(DataDefinitionService dataDefinitionService) {
        this.dataDefinitionService = dataDefinitionService;
    }

    public void validateUniqueActiveCombination(final DataDefinition dataDefinition, final Entity entity,
                                                final String productField, final String productIdField,
                                                final String activeField, final String errorMessageKey) {
        if (!entity.isValid()) {
            return;
        }

        final Long productId = entity.getBelongsToField(productField).getId();
        final Long currentId = entity.getId();
        final Boolean deleted = entity.getBooleanField(GlobalFields.DELETED);
        final Boolean active = entity.getBooleanField(activeField);

        // ⚠️ Nếu đang xóa mềm -> không cần validate
        if (Boolean.TRUE.equals(deleted)) {
            return;
        }

        // 🔍 Tìm xem có bản ghi khác (cùng product) đang active=true, deleted=false
        SearchCriteriaBuilder scb = dataDefinition.find()
                .add(SearchRestrictions.eq(GlobalFields.DELETED, false))
                .add(SearchRestrictions.eq(productIdField, productId))
                .add(SearchRestrictions.eq(activeField, true));

        if (currentId != null) {
            scb.add(SearchRestrictions.ne("id", currentId));
        }

        Entity exists = scb.setMaxResults(1).uniqueResult();

        // 🧠 Nghiệp vụ:
        // Nếu bản ghi hiện tại đang active=true → không được trùng product
        // Nếu bản ghi hiện tại đang active=false → cũng không được trùng với bản ghi active=true
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

            boolean hasActiveLine = false;

            if (lines != null) {
                for (Entity line : lines) {
                    Boolean deleted = line.getBooleanField(GlobalFields.DELETED);
                    if (deleted == null || !deleted) {
                        hasActiveLine = true;
                        break;
                    }
                }
            }

            entity.setField(statusTextField, hasActiveLine ? statusHasStandard : statusNoStandard);
        }

        grid.setEntities(entities);
    }

}

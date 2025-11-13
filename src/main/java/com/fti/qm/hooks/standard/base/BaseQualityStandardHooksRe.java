package com.fti.qm.hooks.standard.base;

import com.fti.qm.constants.GlobalFields;
import com.fti.qm.constants.QSHFields;
import com.fti.qm.constants.equipmentQualityStandardH.EquipmentQualityStandardHFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.constants.QcadooViewConstants;

import java.util.List;

public class BaseQualityStandardHooksRe {

    private final DataDefinitionService dataDefinitionService;

    protected BaseQualityStandardHooksRe(DataDefinitionService dataDefinitionService) {
        this.dataDefinitionService = dataDefinitionService;
    }

    public void validateUniqueActiveCombination(final DataDefinition dataDefinition, final Entity entity,
                                                final String productField, final String productIdField,
                                                final String activeField, final String typeValue,
                                                final String errorMessageKey) {

        if (!entity.isValid()) {
            return;
        }

        final Long productId = entity.getBelongsToField(productField).getId();
        final Long currentId = entity.getId();
        final Boolean deleted = entity.getBooleanField(GlobalFields.DELETED);

        // ⚠️ Nếu bản ghi đang bị xóa mềm thì không cần validate
        if (Boolean.TRUE.equals(deleted)) {
            return;
        }

        // 🔍 Tìm xem có bản ghi khác cùng product + type đang active
        SearchCriteriaBuilder scb = dataDefinition.find()
                .add(SearchRestrictions.eq(QSHFields.TYPE, typeValue))
                .add(SearchRestrictions.eq(GlobalFields.DELETED, false))
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

    public void validateUniqueActiveToolInspectionType(final DataDefinition dataDefinition, final Entity entity,
                                                       final String toolField, final String toolIdField,
                                                       final String inspectionTypeField, final String activeField,
                                                       final String messageKey) {
        if (!entity.isValid()) {
            return;
        }

        Long toolId = entity.getBelongsToField(toolField).getId();
        String inspectionType = entity.getStringField(inspectionTypeField);
        Long currentId = entity.getId();
        final Boolean deleted = entity.getBooleanField(GlobalFields.DELETED);

        if (Boolean.TRUE.equals(deleted)) {
            return;
        }

        // 🔍 Kiểm tra tồn tại bản ghi khác cùng tool + inspectionType đang active
        SearchCriteriaBuilder scb = dataDefinition.find()
                .add(SearchRestrictions.eq(GlobalFields.DELETED, false))
                .add(SearchRestrictions.eq(toolIdField, toolId))
                .add(SearchRestrictions.eq(inspectionTypeField, inspectionType))
                .add(SearchRestrictions.eq(activeField, true));

        if (currentId != null) {
            scb.add(SearchRestrictions.ne("id", currentId));
        }

        Entity exists = scb.setMaxResults(1).uniqueResult();

        if (exists != null) {
            entity.addError(dataDefinition.getField(toolField), messageKey);
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

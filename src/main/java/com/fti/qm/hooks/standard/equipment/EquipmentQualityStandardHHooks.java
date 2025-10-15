package com.fti.qm.hooks.standard.equipment;

import com.fti.qm.constants.equipmentQualityStandardH.EquipmentQualityStandardHFields;
import com.fti.qm.hooks.standard.base.BaseQualityStandardHooks;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ViewDefinitionState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EquipmentQualityStandardHHooks extends BaseQualityStandardHooks {
    public static final String DUPLICATE_ACTIVE_TOOL_INSPECTION_TYPE = "qm.equipmentQualityStandardH.error.duplicateActiveToolInspectionType";
    @Autowired
    public EquipmentQualityStandardHHooks(DataDefinitionService dataDefinitionService) {
        super(dataDefinitionService);
    }

    public void validateUniqueActiveToolInspectionType(final DataDefinition dataDefinition, final Entity entity) {
        if (!entity.isValid()) {
            return;
        }

        Long toolId = entity.getBelongsToField(EquipmentQualityStandardHFields.TOOL).getId();
        String inspectionType = entity.getStringField(EquipmentQualityStandardHFields.INSPECTION_TYPE);
        Long currentId = entity.getId();

        SearchCriteriaBuilder scb = dataDefinition.find()
                .add(SearchRestrictions.eq(EquipmentQualityStandardHFields.TOOL_ID, toolId))
                .add(SearchRestrictions.eq(EquipmentQualityStandardHFields.INSPECTION_TYPE, inspectionType))
                .add(SearchRestrictions.eq(EquipmentQualityStandardHFields.ACTIVE, true));

        if (currentId != null) {
            scb.add(SearchRestrictions.ne("id", currentId));
        }

        Entity exists = scb.setMaxResults(1).uniqueResult();

        if (exists != null) {
            entity.addError(dataDefinition.getField(EquipmentQualityStandardHFields.TOOL), DUPLICATE_ACTIVE_TOOL_INSPECTION_TYPE);
        }
    }

    public void setStatusText(final ViewDefinitionState view) {
        handleSetStatusText(view,
                EquipmentQualityStandardHFields.EQUIPMENT_QUALITY_STANDARD_LS,
                EquipmentQualityStandardHFields.STATUS_TEXT,
                EquipmentQualityStandardHFields.STATUS_NO_STANDARD,
                EquipmentQualityStandardHFields.STATUS_HAS_STANDARD);
    }
}

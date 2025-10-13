package com.fti.qm.hooks.standard.equipment;

import com.fti.qm.constants.equipmentQualityStandardH.EquipmentQualityStandardHFields;
import com.fti.qm.hooks.standard.base.BaseQualityStandardHooks;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.view.api.ViewDefinitionState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EquipmentQualityStandardHHooks extends BaseQualityStandardHooks {
    @Autowired
    public EquipmentQualityStandardHHooks(DataDefinitionService dataDefinitionService) {
        super(dataDefinitionService);
    }

    public void setStatusText(final ViewDefinitionState view) {
        handleSetStatusText(view,
                EquipmentQualityStandardHFields.EQUIPMENT_QUALITY_STANDARD_LS,
                EquipmentQualityStandardHFields.STATUS_TEXT,
                EquipmentQualityStandardHFields.STATUS_NO_STANDARD,
                EquipmentQualityStandardHFields.STATUS_HAS_STANDARD);
    }
}

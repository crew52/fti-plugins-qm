package com.fti.qm.listeners.standard;

import com.fti.qm.constants.equipmentQualityStandardH.EquipmentQualityStandardLFields;
import com.fti.qm.listeners.standard.base.BaseQualityStandardDetailsListener;
import org.springframework.stereotype.Service;

@Service
public class EQSLDetailsListeners extends BaseQualityStandardDetailsListener{
    @Override public String QUALITY_CRITERIA() { return EquipmentQualityStandardLFields.QUALITY_CRITERIA; }
    @Override public String UNIT() { return EquipmentQualityStandardLFields.UNIT; }
    @Override public String QUANTITATIVE_VALUE() { return EquipmentQualityStandardLFields.QUANTITATIVE_VALUE; }
    @Override public String TOLERANCE() { return EquipmentQualityStandardLFields.TOLERANCE; }
    @Override public String UP_VALUE() { return EquipmentQualityStandardLFields.UP_VALUE; }
    @Override public String DOWN_VALUE() { return EquipmentQualityStandardLFields.DOWN_VALUE; }
}

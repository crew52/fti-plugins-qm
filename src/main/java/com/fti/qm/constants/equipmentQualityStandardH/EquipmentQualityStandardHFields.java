package com.fti.qm.constants.equipmentQualityStandardH;

public class EquipmentQualityStandardHFields {
    private EquipmentQualityStandardHFields() {

    }
    public static final String ACTIVE = "active";
    public static final String TOOL = "tool";
    public static final String TOOL_ID = TOOL + ".id";

    public static final String STATUS_TEXT = "statusText";
    public static final String STATUS_NO_STANDARD = "01noStandard";
    public static final String STATUS_HAS_STANDARD = "02hasStandard";

    public static final String INSPECTION_TYPE = "inspectionType";
    public static final String INSPECTION_TYPE_EQUIPMENT = "01equipment";
    public static final String INSPECTION_TYPE_INTERNAL_CALIBRATION = "02internalCalibration";
    public static final String INSPECTION_TYPE_EXTERNAL_CALIBRATION = "03externalCalibration";

    public static final String EQUIPMENT_QUALITY_STANDARD_LS = "equipmentQualityStandardLs";
}

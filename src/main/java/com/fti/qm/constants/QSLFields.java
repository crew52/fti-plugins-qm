package com.fti.qm.constants;

public final class QSLFields {
    private QSLFields() {

    }
    // String fields
    public static final String POSITION = "position";
    public static final String DESCRIPTION = "description";

    // Enum field
    public static final String QUALITATIVE_VALUE = "qualitativeValue";

    // Non-persistent boolean (UI checkbox)
    public static final String QUALITATIVE_CHECKBOX = "qualitativeCheckbox";

    // Decimal fields
    public static final String QUANTITATIVE_VALUE = "quantitativeValue";
    public static final String TOLERANCE = "tolerance";
    public static final String UP_VALUE = "upValue";
    public static final String DOWN_VALUE = "downValue";

    // Integer fields
    public static final String SAMPLE_SIZE = "sampleSize";

    // Dictionary field
    public static final String UNIT = "unit";

    // BelongsTo fields
    public static final String QUALITY_CRITERIA = "qualityCriteria";
    public static final String MEASURING_EQUIPMENT = "measuringEquipment";
    public static final String QUALITY_STANDARD_H = "qualityStandardH";
    public static final String QUALITY_STANDARD_H_ID = QUALITY_STANDARD_H + ".id";

    // Soft delete fields
    public static final String DELETED = "deleted";
    public static final String DELETED_DATE = "deletedDate";

    public static final class QualitativeValue {
        private QualitativeValue() {}

        public static final String PASS = "01pass";
    }
}

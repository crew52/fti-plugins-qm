package com.fti.qm.constants;

public final class QSHFields {
    private QSHFields() {

    }
    public static final String TYPE = "type";

    public static final String INSPECTION_TYPE = "inspectionType";

    public static final String STATUS_TEXT = "statusText";

    public static final String DELETED = "deleted";

    public static final String DELETED_DATE = "deletedDate";

    public static final String PRODUCT = "product";

    public static final String PRODUCT_ID = PRODUCT + ".id";

    public static final String TOOL = "tool";

    public static final String TOOL_ID = TOOL + ".id";

    public static final String QUALITY_STANDARD_LS = "qualityStandardLs";

    public static final String ATTACHMENTS = "attachments";

    public static final String ACTIVE = "active";

    public static final class Type {
        private Type() {}

        public static final String INCOMING = "01incoming";
        public static final String INPROCESS = "02inprocess";
        public static final String OUTGOING = "03outgoing";
        public static final String EQUIPMENT = "04equipment";
    }

    public static final class InspectionType {
        private InspectionType() {}

        public static final String EQUIPMENT = "01equipment";
        public static final String INTERNAL_CALIBRATION = "02internalCalibration";
        public static final String EXTERNAL_CALIBRATION = "03externalCalibration";
    }

    public static final class StatusText {
        private StatusText() {}

        public static final String NO_STANDARD = "01noStandard";
        public static final String HAS_STANDARD = "02hasStandard";
    }

}

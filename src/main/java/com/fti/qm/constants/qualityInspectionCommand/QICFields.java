package com.fti.qm.constants.qualityInspectionCommand;

public final class QICFields {
    private QICFields() {
    }

    // Fields
    public static final String QUALITY_INSPECTION_COMMAND_ID = "qualityInspectionCommandRe.id";
    public static final String INSPECTION_TYPE = "inspectionType";
    public static final String INSPECTION_TYPE_INCOMING = "01incoming";
    public static final String INSPECTION_TYPE_IN_PROCESS = "02inprocess";
    public static final String INSPECTION_TYPE_OUTGOING = "03outgoing";
    public static final String INSPECTION_TYPE_EQUIPMENT = "04equipment";
    public static final String STATUS = "status";
    public static final String STATUS_NEW = "01new";
    public static final String STATUS_IN_PROGRESS = "02inProgress";
    public static final String STATUS_COMPLETED = "03completed";

    public static final String PRODUCTION_ORDER_NUMBER = "productionOrderNumber";
    public static final String OPERATION_NUMBER = "operationNumber";
    public static final String COMPANY = "company";
    public static final String PRODUCT = "product";
    public static final String TOOL = "tool";
    public static final String INSPECTION_ORDER_NUMBER = "inspectionOrderNumber";
    public static final String INSPECTION_ORDER_NUMBER_INT = "inspectionOrderNumberInt";
    public static final String PO_NUMBER = "poNumber";
    public static final String EXECUTION_DATE = "executionDate";
    public static final String TRANSACTION_QUANTITY = "transactionQuantity";

    public static final String QUALITY_DECISION = "qualityDecision";
    public static final String QUALITY_DECISION_CHECKBOX = "qualityDecisionCheckBox";
    public static final String WAREHOUSE_QUANTITY = "warehouseQuantity";
    public static final String NG_QUANTITY = "ngQuantity";
    public static final String WAREHOUSE_LOCATION = "warehouseLocation";
    public static final String NG_LOCATION = "ngLocation";
    public static final String LOCATION = "location";
    public static final String QUALITY_INSPECTION_COMMAND_CONTEXT = "qualityInspectionCommandContextRe";
    public static final String USER = "user";

    public static final String INSPECTION_DATE = "inspectionDate";
    public static final String NOTE = "note";

    public static final String VERSION = "version";
    public static final String CREATED_DATE = "createdDate";

    public static final class QualityDecision {
        private QualityDecision() {}

        public static final String ACCEPT = "01accept";
        public static final String REJECT = "02reject";
        public static final String PARTIAL = "03partial";
    }
}

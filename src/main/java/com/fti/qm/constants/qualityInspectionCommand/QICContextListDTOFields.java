package com.fti.qm.constants.qualityInspectionCommand;

public final class QICContextListDTOFields {
    private QICContextListDTOFields() {
    }
    // === Enum fields ===
    public static final String INSPECTION_TYPE = "inspectionType";
    public static final String STATUS = "status";

    // === Date fields ===
    public static final String EXECUTION_DATE = "executionDate";

    // === String fields ===
    public static final String PRODUCTION_ORDER_NUMBER = "productionOrderNumber";
    public static final String OPERATION_NUMBER = "operationNumber";
    public static final String COMPANY_NUMBER = "companyNumber";
    public static final String COMPANY_NAME = "companyName";
    public static final String PRODUCT_NUMBER = "productNumber";
    public static final String PRODUCT_NAME = "productName";
    public static final String TOOL_NUMBER = "toolNumber";
    public static final String TOOL_NAME = "toolName";
    public static final String INSPECTION_ORDER_NUMBER = "inspectionOrderNumber";
    public static final String PO_NUMBER = "poNumber";
    public static final String WAREHOUSE = "warehouse";

    // === Integer / BelongsTo fields ===
    public static final String COMPANY_ID = "company_id";
    public static final String PRODUCT_ID = "product_id";
    public static final String TOOL_ID = "tool_id";

    // === Decimal fields ===
    public static final String TRANSACTION_QUANTITY = "transactionQuantity";
}

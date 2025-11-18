package com.fti.qm.validators;

import com.fti.qm.constants.qualityInspectionCommand.QICFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.FieldDefinition;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class QICDetailsValidationHooks {

    public static final String ERROR_WAREHOUSE_QTY = "qm.error.warehouseQuantity.invalid";
    public static final String ERROR_NG_QTY = "qm.error.ngQuantity.invalid";

    // Method validateWarehouseQuantity
    public boolean validateWarehouseQuantity(final DataDefinition dataDefinition,
                                             final FieldDefinition fieldDefinition,
                                             final Entity entity,
                                             final Object oldValue,
                                             final Object newValue) {
        BigDecimal transactionQty = entity.getDecimalField(QICFields.TRANSACTION_QUANTITY);
        BigDecimal warehouseQty = newValue != null ? new BigDecimal(newValue.toString()) : BigDecimal.ZERO;
        BigDecimal ngQty = entity.getDecimalField(QICFields.NG_QUANTITY) != null
                ? entity.getDecimalField(QICFields.NG_QUANTITY) : BigDecimal.ZERO;

        if (transactionQty != null && warehouseQty.add(ngQty).compareTo(transactionQty) > 0) {
            entity.addError(fieldDefinition, ERROR_WAREHOUSE_QTY);
            return false;
        }

        return true;
    }

    // Method validateNgQuantity
    public boolean validateNgQuantity(final DataDefinition dataDefinition,
                                      final FieldDefinition fieldDefinition,
                                      final Entity entity,
                                      final Object oldValue,
                                      final Object newValue) {
        BigDecimal transactionQty = entity.getDecimalField(QICFields.TRANSACTION_QUANTITY);
        BigDecimal ngQty = newValue != null ? new BigDecimal(newValue.toString()) : BigDecimal.ZERO;
        BigDecimal warehouseQty = entity.getDecimalField(QICFields.WAREHOUSE_QUANTITY) != null
                ? entity.getDecimalField(QICFields.WAREHOUSE_QUANTITY) : BigDecimal.ZERO;

        if (transactionQty != null && warehouseQty.add(ngQty).compareTo(transactionQty) > 0) {
            entity.addError(fieldDefinition, ERROR_NG_QTY);
            return false;
        }

        return true;
    }
}
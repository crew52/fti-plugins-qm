package com.fti.qm.validators;

import com.fti.qm.constants.qualityInspectionCommand.QICFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.FieldDefinition;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.function.BiFunction;

/**
 * Lớp hooks dùng để validate các trường và quy tắc nghiệp vụ cho chi tiết Lệnh Kiểm Tra Chất Lượng (QIC).
 *
 * <p>
 * Chứa các phương thức validate:
 * <ul>
 *     <li>Số lượng kho (warehouseQuantity) và số lượng NG (ngQuantity) phải phù hợp với số lượng giao dịch (transactionQuantity)</li>
 *     <li>Các trường bắt buộc hoặc phải rỗng tùy theo quyết định chất lượng (qualityDecision)</li>
 * </ul>
 * </p>
 */
@Service
public class QICDetailsValidationHooks {

    public static final String ERROR_WAREHOUSE_QTY = "qm.error.warehouseQuantity.invalid";
    public static final String ERROR_NG_QTY = "qm.error.ngQuantity.invalid";

    /**
     * Validate số lượng nhập kho (warehouseQuantity) không được vượt quá số lượng giao dịch trừ đi số lượng NG.
     *
     * @param dataDefinition  DataDefinition của entity
     * @param fieldDefinition FieldDefinition của trường warehouseQuantity
     * @param entity          Entity cần validate
     * @param oldValue        Giá trị cũ của trường warehouseQuantity
     * @param newValue        Giá trị mới của trường warehouseQuantity
     * @return true nếu hợp lệ, false nếu tổng warehouseQuantity + ngQuantity > transactionQuantity
     */
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

    /**
     * Validate số lượng NG (ngQuantity) không được vượt quá số lượng giao dịch trừ đi số lượng nhập kho.
     *
     * @param dataDefinition  DataDefinition của entity
     * @param fieldDefinition FieldDefinition của trường ngQuantity
     * @param entity          Entity cần validate
     * @param oldValue        Giá trị cũ của trường ngQuantity
     * @param newValue        Giá trị mới của trường ngQuantity
     * @return true nếu hợp lệ, false nếu tổng warehouseQuantity + ngQuantity > transactionQuantity
     */
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

    /**
     * Validate các trường liên quan đến quyết định chất lượng (qualityDecision) dựa trên quy tắc nghiệp vụ:
     *
     * <ul>
     *     <li>ACCEPT: warehouseQuantity & warehouseLocation bắt buộc, ngQuantity & ngLocation phải rỗng</li>
     *     <li>REJECT: ngQuantity & ngLocation bắt buộc, warehouseQuantity & warehouseLocation phải rỗng</li>
     *     <li>PARTIAL: tất cả 4 trường đều bắt buộc</li>
     * </ul>
     *
     * @param dd     DataDefinition của entity
     * @param entity Entity cần validate
     * @return true nếu tất cả các trường thỏa mãn quy tắc, false nếu có lỗi
     */
    public boolean validateDecisionDependencies(final DataDefinition dd, final Entity entity) {

        String decision = entity.getStringField(QICFields.QUALITY_DECISION);
        if (decision == null) {
            return true;
        }

        boolean valid = true;

        // Helper lambda
        BiFunction<String, String, Boolean> required = (field, error) -> {
            if (entity.getField(field) == null) {
                entity.addError(dd.getField(field), error);
                return false;
            }
            return true;
        };

        BiFunction<String, String, Boolean> mustNull = (field, error) -> {
            if (entity.getField(field) != null) {
                entity.addError(dd.getField(field), error);
                return false;
            }
            return true;
        };

        switch (decision) {

            case QICFields.QualityDecision.ACCEPT:
                valid &= required.apply(QICFields.WAREHOUSE_QUANTITY, "qm.qic.error.warehouseQuantityRequired");
                valid &= required.apply(QICFields.WAREHOUSE_LOCATION, "qm.qic.error.warehouseLocationRequired");

                valid &= mustNull.apply(QICFields.NG_QUANTITY, "qm.qic.error.ngQuantityMustBeNull");
                valid &= mustNull.apply(QICFields.NG_LOCATION, "qm.qic.error.ngLocationMustBeNull");
                break;

            case QICFields.QualityDecision.REJECT:
                valid &= required.apply(QICFields.NG_QUANTITY, "qm.qic.error.ngQuantityRequired");
                valid &= required.apply(QICFields.NG_LOCATION, "qm.qic.error.ngLocationRequired");

                valid &= mustNull.apply(QICFields.WAREHOUSE_QUANTITY, "qm.qic.error.warehouseQuantityMustBeNull");
                valid &= mustNull.apply(QICFields.WAREHOUSE_LOCATION, "qm.qic.error.warehouseLocationMustBeNull");
                break;

            case QICFields.QualityDecision.PARTIAL:
                valid &= required.apply(QICFields.WAREHOUSE_QUANTITY, "qm.qic.error.warehouseQuantityRequired");
                valid &= required.apply(QICFields.WAREHOUSE_LOCATION, "qm.qic.error.warehouseLocationRequired");

                valid &= required.apply(QICFields.NG_QUANTITY, "qm.qic.error.ngQuantityRequired");
                valid &= required.apply(QICFields.NG_LOCATION, "qm.qic.error.ngLocationRequired");
                break;
        }

        return valid;
    }

}
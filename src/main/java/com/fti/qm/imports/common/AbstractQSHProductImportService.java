package com.fti.qm.imports.common;

import com.fti.qm.constants.QSHFields;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basic.imports.services.XlsxImportService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.FieldDefinition;

/**
 * Base class dùng để xử lý nghiệp vụ import Product cho các loại QSH (Quality Standard Header),
 * bao gồm Incoming (IQSH), Outgoing (OQSH), InProcess (PQSH), ...
 *
 * <p>Lớp này triển khai Template Method Pattern, cung cấp các phần xử lý chung như:
 * <ul>
 *     <li>Kiểm tra product bắt buộc phải nhập.</li>
 *     <li>Xác minh product phải tồn tại trong database.</li>
 *     <li>Validate loại nguyên vật liệu (globalTypeOfMaterial).</li>
 *     <li>Gán lại giá trị product hợp lệ vào entity.</li>
 * </ul>
 *
 * <p>Các lớp con chỉ cần override:
 * <ul>
 *     <li>{@link #setDefaultType(Entity)} – thiết lập type mặc định.</li>
 *     <li>{@link #isMaterialTypeValid(String)} – định nghĩa các loại nguyên vật liệu hợp lệ.</li>
 *     <li>{@link #getInvalidMaterialTypeErrorCode()} – mã lỗi khi loại nguyên vật liệu không hợp lệ.</li>
 * </ul>
 *
 * <p>Nhờ cấu trúc này, toàn bộ logic validate chung được gom lại một nơi,
 * giúp giảm trùng lặp code giữa IQSH, OQSH, PQSH và tăng khả năng mở rộng.
 */
public abstract class AbstractQSHProductImportService extends XlsxImportService {
    /** Lỗi: Product không được để trống. */
    private static final String ERROR_PRODUCT_REQUIRED   = "qm.qsh.error.product.required";
    /** Lỗi: Product không tồn tại trong database. */
    private static final String ERROR_PRODUCT_NOT_FOUND  = "qm.qsh.error.product.notFound";
    /** Lỗi: Product import không phải kiểu Entity. */
    private static final String ERROR_PRODUCT_INVALID_TYPE = "qm.qsh.product.invalidType";

    /** Lỗi: Tên product không khớp với mã product. */
    private static final String ERROR_PRODUCT_NAME_NOT_MATCH = "qm.qsh.product.name.notMatch";

    /** Tên field trong Product: Loại nguyên vật liệu (component, intermediate, finalProduct, ...). */
    protected static final String PRODUCT_FIELD_GLOBAL_TYPE_OF_MATERIAL = "globalTypeOfMaterial";

    /**
     * Validate cho từng dòng import từ Excel. Đây là entry point chính khi import.
     *
     * <p>Gồm 2 bước:
     * <ul>
     *     <li>Gán type mặc định (INCOMING, OUTGOING, ...).</li>
     *     <li>Validate product theo quy tắc chung và riêng từng loại QSH.</li>
     * </ul>
     *
     * @param entity         Entity được tạo từ dòng Excel
     * @param dataDefinition DataDefinition của model QSH
     */
    @Override
    public void validateEntity(Entity entity, DataDefinition dataDefinition) {
        setDefaultType(entity);
        validateProduct(entity, dataDefinition);
    }

    /**
     * Thiết lập type mặc định khi người dùng không nhập.
     *
     * <p>Mỗi lớp con sẽ tự gán:
     * <ul>
     *     <li>IQSH → type = INCOMING</li>
     *     <li>OQSH → type = OUTGOING</li>
     *     <li>PQSH → type = IN_PROCESS</li>
     * </ul>
     *
     * @param entity entity đang được xử lý
     */
    protected abstract void setDefaultType(Entity entity);

    /**
     * Kiểm tra xem giá trị globalTypeOfMaterial có hợp lệ cho từng loại QSH hay không.
     *
     * <p>Ví dụ:
     * <ul>
     *     <li>IQSH: chỉ chấp nhận 01component hoặc 02intermediate.</li>
     *     <li>OQSH: chỉ chấp nhận 03finalProduct.</li>
     *     <li>PQSH: có thể áp dụng logic riêng.</li>
     * </ul>
     *
     * @param materialType giá trị globalTypeOfMaterial của Product
     * @return true nếu hợp lệ, false nếu không hợp lệ
     */
    protected abstract boolean isMaterialTypeValid(String materialType);

    /**
     * Mã lỗi được hiển thị khi giá trị globalTypeOfMaterial không hợp lệ.
     *
     * <p>Ví dụ:
     * <ul>
     *     <li>qm.iqsh.product.invalidMaterialType</li>
     *     <li>qm.oqsh.product.invalidMaterialType</li>
     * </ul>
     *
     * @return message key lỗi
     */
    protected abstract String getInvalidMaterialTypeErrorCode();

    /**
     * Validate trường Product cho một dòng import QSH.
     *
     * <p>Thực hiện các kiểm tra:
     * <ul>
     *     <li>Product bắt buộc và phải là Entity.</li>
     *     <li>Product phải tồn tại trong database.</li>
     *     <li>So khớp tên Product (nếu có nhập).</li>
     *     <li>Validate loại nguyên vật liệu theo từng QSH.</li>
     * </ul>
     *
     * @param entity Entity được tạo từ dòng Excel
     * @param dataDefinition DataDefinition của model QSH
     */
    protected void validateProduct(Entity entity, DataDefinition dataDefinition) {

        FieldDefinition productField = dataDefinition.getField(QSHFields.PRODUCT);
        Object rawValue = entity.getField(QSHFields.PRODUCT);

        // 1. Product bắt buộc
        if (rawValue == null) {
            entity.addError(productField, ERROR_PRODUCT_REQUIRED);
            return;
        }

        // 2. Product phải là Entity
        if (!(rawValue instanceof Entity)) {
            entity.addError(productField, ERROR_PRODUCT_INVALID_TYPE);
            return;
        }

        Entity productEntity = (Entity) rawValue;

        // 2.1 Product phải tồn tại DB
        if (productEntity.getId() == null) {
            entity.addError(productField, ERROR_PRODUCT_NOT_FOUND);
            return;
        }

        // 2.2 So khớp tên product
        if (!validateProductNameMatch(entity, dataDefinition, productEntity)) {
            return;
        }

        // 2.3 Validate loại material
        String globalType = productEntity.getStringField(PRODUCT_FIELD_GLOBAL_TYPE_OF_MATERIAL);

        if (!isMaterialTypeValid(globalType)) {
            entity.addError(productField, getInvalidMaterialTypeErrorCode());
            return;
        }

        // 2.4 Set lại product hợp lệ
        entity.setField(QSHFields.PRODUCT, productEntity);
    }

    /**
     * So khớp tên Product nhập từ Excel với Product trong hệ thống.
     *
     * <p>Nếu không nhập tên → bỏ qua.
     * Nếu không khớp → gắn lỗi tại field IMPORT_PRODUCT_NAME.
     *
     * @return true nếu hợp lệ hoặc không cần validate, false nếu có lỗi
     */
    protected boolean validateProductNameMatch(
            Entity entity,
            DataDefinition dataDefinition,
            Entity productEntity) {

        String importProductName = entity.getStringField(QSHFields.IMPORT_PRODUCT_NAME);
        String productName = productEntity.getStringField(ProductFields.NAME);

        if (importProductName != null && productName != null) {
            if (!productName.trim().equalsIgnoreCase(importProductName.trim())) {

                FieldDefinition importNameField = dataDefinition.getField(QSHFields.IMPORT_PRODUCT_NAME);

                entity.addError(importNameField, ERROR_PRODUCT_NAME_NOT_MATCH);
                return false;
            }
        }

        return true;
    }
}
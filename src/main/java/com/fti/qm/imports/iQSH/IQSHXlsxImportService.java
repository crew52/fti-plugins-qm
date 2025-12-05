package com.fti.qm.imports.iQSH;

import com.fti.qm.constants.QSHFields;
import com.qcadoo.mes.basic.imports.services.XlsxImportService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.FieldDefinition;
import org.springframework.stereotype.Service;

/**
 * Xử lý import IQSH từ file XLSX.
 * <p>
 * Thực hiện:
 * <ul>
 *     <li>Gán mặc định type = 01incoming.</li>
 *     <li>Validate trường product được import từ Excel.</li>
 * </ul>
 */
@Service
public class IQSHXlsxImportService extends XlsxImportService {

    private static final String ERROR_PRODUCT_REQUIRED   = "qm.iqsh.error.product.required";
    private static final String ERROR_PRODUCT_NOT_FOUND  = "qm.iqsh.error.product.notFound";
    private static final String ERROR_PRODUCT_INVALID_TYPE = "qm.iqsh.product.invalidType";
    private static final String ERROR_PRODUCT_INVALID_TYPE_OF_MATERIAL = "qm.iqsh.product.invalidMaterialType";

    private static final String PRODUCT_FIELD_GLOBAL_TYPE_OF_MATERIAL = "globalTypeOfMaterial";
    private static final String VALUE_COMPONENT = "01component";
    private static final String VALUE_INTERMEDIATE = "02intermediate";

    /**
     * Validate entity import:
     * <ul>
     *     <li>Đặt type mặc định.</li>
     *     <li>Kiểm tra product.</li>
     * </ul>
     */
    @Override
    public void validateEntity(Entity entity, DataDefinition dataDefinition) {
        setDefaultType(entity, dataDefinition);
        validateProduct(entity, dataDefinition);
    }

    /**
     * Gán type = 01incoming nếu chưa được nhập.
     */
    private void setDefaultType(Entity entity, DataDefinition dataDefinition) {
        if (entity.getField(QSHFields.TYPE) == null) {
            entity.setField(QSHFields.TYPE, QSHFields.Type.INCOMING);
        }
    }

    /**
     * Kiểm tra product:
     * <ul>
     *     <li>Bắt buộc nhập.</li>
     *     <li>Phải là Entity hợp lệ.</li>
     *     <li>globalTypeOfMaterial phải là 01component hoặc 02intermediate.</li>
     * </ul>
     */
    private void validateProduct(Entity entity, DataDefinition dataDefinition) {

        FieldDefinition productField = dataDefinition.getField("product");
        Object rawValue = entity.getField("product");

        // 1) Không nhập gì
        if (rawValue == null) {
            entity.addError(productField, ERROR_PRODUCT_REQUIRED);
            return;
        }

        // 2) Đã truyền đúng Product Entity từ XLSX
        if (rawValue instanceof Entity) {

            Entity productEntity = (Entity) rawValue;

            // 2.1) Product không tồn tại trong DB
            if (productEntity.getId() == null) {
                entity.addError(productField, ERROR_PRODUCT_NOT_FOUND);
                return;
            }

            // 2.2) Kiểm tra globalTypeOfMaterial
            String globalType = productEntity.getStringField(PRODUCT_FIELD_GLOBAL_TYPE_OF_MATERIAL);

            if (!VALUE_COMPONENT.equals(globalType) && !VALUE_INTERMEDIATE.equals(globalType)) {
                entity.addError(productField, ERROR_PRODUCT_INVALID_TYPE_OF_MATERIAL);
                return;
            }

            // 2.3) Hợp lệ ⇒ gán
            entity.setField("product", productEntity);
            return;
        }

        // 3) Trường hợp dữ liệu không đúng định dạng (không phải Entity)
        entity.addError(productField, ERROR_PRODUCT_INVALID_TYPE);
    }
}

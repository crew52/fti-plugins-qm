package com.fti.qm.imports.qshProduct;

import com.fti.qm.constants.QSHFields;
import com.qcadoo.mes.basic.imports.services.XlsxImportService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.FieldDefinition;
import org.springframework.stereotype.Service;


@Service
public class OQSHXlsxImportService extends XlsxImportService {

    private static final String ERROR_PRODUCT_REQUIRED   = "qm.iqsh.error.product.required";
    private static final String ERROR_PRODUCT_NOT_FOUND  = "qm.iqsh.error.product.notFound";
    private static final String ERROR_PRODUCT_INVALID_TYPE = "qm.iqsh.product.invalidType";
    private static final String ERROR_PRODUCT_INVALID_TYPE_OF_MATERIAL = "qm.oqsh.product.invalidMaterialType";
    private static final String PRODUCT_FIELD_GLOBAL_TYPE_OF_MATERIAL = "globalTypeOfMaterial";
    private static final String VALUE_FINAL_PRODUCT = "03finalProduct";

    @Override
    public void validateEntity(Entity entity, DataDefinition dataDefinition) {
        setDefaultType(entity, dataDefinition);
        validateProduct(entity, dataDefinition);
    }

    private void setDefaultType(Entity entity, DataDefinition dataDefinition) {
        if (entity.getField(QSHFields.TYPE) == null) {
            entity.setField(QSHFields.TYPE, QSHFields.Type.OUTGOING);
        }
    }

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

            if (!VALUE_FINAL_PRODUCT.equals(globalType)) {
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

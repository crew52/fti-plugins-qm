package com.fti.qm.imports.qshProduct;

import com.fti.qm.constants.QSHFields;
import com.qcadoo.model.api.Entity;
import org.springframework.stereotype.Service;

@Service
public class OQSHXlsxImportService extends AbstractQSHProductImportService  {

    private static final String ERROR_PRODUCT_INVALID_TYPE_OF_MATERIAL = "qm.oqsh.product.invalidMaterialType";
    private static final String VALUE_FINAL_PRODUCT = "03finalProduct";

    @Override
    protected void setDefaultType(Entity entity) {
        if (entity.getField(QSHFields.TYPE) == null) {
            entity.setField(QSHFields.TYPE, QSHFields.Type.OUTGOING);
        }
    }

    @Override
    protected boolean isMaterialTypeValid(String materialType) {
        return VALUE_FINAL_PRODUCT.equals(materialType);
    }

    @Override
    protected String getInvalidMaterialTypeErrorCode() {
        return ERROR_PRODUCT_INVALID_TYPE_OF_MATERIAL;
    }
}

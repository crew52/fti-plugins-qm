package com.fti.qm.imports.qshProduct;

import com.fti.qm.constants.QSHFields;
import com.fti.qm.imports.common.AbstractQSHProductImportService;
import com.qcadoo.model.api.Entity;
import org.springframework.stereotype.Service;

@Service
public class IQSHXlsxImportService extends AbstractQSHProductImportService {

    private static final String ERROR_PRODUCT_INVALID_TYPE_OF_MATERIAL = "qm.iqsh.product.invalidMaterialType";
    private static final String VALUE_COMPONENT = "01component";
    private static final String VALUE_INTERMEDIATE = "02intermediate";

    @Override
    protected void setDefaultType(Entity entity) {
        if (entity.getField(QSHFields.TYPE) == null) {
            entity.setField(QSHFields.TYPE, QSHFields.Type.INCOMING);
        }
    }

    @Override
    protected boolean isMaterialTypeValid(String materialType) {
        return VALUE_COMPONENT.equals(materialType)
                || VALUE_INTERMEDIATE.equals(materialType);
    }

    @Override
    protected String getInvalidMaterialTypeErrorCode() {
        return ERROR_PRODUCT_INVALID_TYPE_OF_MATERIAL;
    }
}

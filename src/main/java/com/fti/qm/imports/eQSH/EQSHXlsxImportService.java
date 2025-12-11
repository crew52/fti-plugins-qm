package com.fti.qm.imports.eQSH;

import com.fti.qm.constants.QSHFields;
import com.qcadoo.mes.basic.imports.services.XlsxImportService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import org.springframework.stereotype.Service;

@Service
public class EQSHXlsxImportService extends XlsxImportService {
    @Override
    public void validateEntity(Entity entity, DataDefinition dataDefinition) {
        setDefaultType(entity);
    }

    protected void setDefaultType(Entity entity) {
        if (entity.getField(QSHFields.TYPE) == null) {
            entity.setField(QSHFields.TYPE, QSHFields.Type.EQUIPMENT);
        }
    }
}

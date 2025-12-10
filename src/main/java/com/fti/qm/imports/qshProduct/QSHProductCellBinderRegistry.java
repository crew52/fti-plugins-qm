package com.fti.qm.imports.qshProduct;

import com.fti.qm.constants.QSHFields;
import com.qcadoo.mes.basic.imports.dtos.CellBinderRegistry;
import com.qcadoo.mes.basic.imports.helpers.CellParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

import static com.qcadoo.mes.basic.imports.dtos.CellBinder.required;

@Component
public class QSHProductCellBinderRegistry {
    private CellBinderRegistry cellBinderRegistry = new CellBinderRegistry();

    public QSHProductCellBinderRegistry() {
    }

    @Autowired
    private CellParser productCellParser;

    @PostConstruct
    private void init() {
        cellBinderRegistry.setCellBinder(required(QSHFields.PRODUCT, productCellParser));
    }

    public CellBinderRegistry getCellBinderRegistry() {
        return this.cellBinderRegistry;
    }
}

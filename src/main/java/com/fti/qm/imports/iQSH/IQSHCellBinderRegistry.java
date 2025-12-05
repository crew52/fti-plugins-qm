package com.fti.qm.imports.iQSH;

import com.fti.qm.constants.QSHFields;
import com.qcadoo.mes.basic.imports.dtos.CellBinderRegistry;
import com.qcadoo.mes.basic.imports.helpers.CellParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import static com.qcadoo.mes.basic.imports.dtos.CellBinder.required;

import javax.annotation.PostConstruct;

@Component
public class IQSHCellBinderRegistry {
    private CellBinderRegistry cellBinderRegistry = new CellBinderRegistry();

    public IQSHCellBinderRegistry() {
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

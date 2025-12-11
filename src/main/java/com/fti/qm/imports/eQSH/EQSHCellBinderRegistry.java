package com.fti.qm.imports.eQSH;

import com.fti.qm.constants.QSHFields;
import com.qcadoo.mes.basic.imports.dtos.CellBinderRegistry;
import com.qcadoo.mes.basic.imports.helpers.CellParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

import static com.qcadoo.mes.basic.imports.dtos.CellBinder.required;

@Component
public class EQSHCellBinderRegistry {
    private CellBinderRegistry cellBinderRegistry = new CellBinderRegistry();

    public EQSHCellBinderRegistry() {
    }

    @Autowired
    private CellParser toolCellParser;

    @PostConstruct
    private void init() {
        cellBinderRegistry.setCellBinder(required(QSHFields.TOOL, toolCellParser));
        cellBinderRegistry.setCellBinder(required(QSHFields.INSPECTION_TYPE));
    }

    public CellBinderRegistry getCellBinderRegistry() {
        return this.cellBinderRegistry;
    }
}

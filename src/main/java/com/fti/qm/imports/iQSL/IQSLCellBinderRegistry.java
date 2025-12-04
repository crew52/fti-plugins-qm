package com.fti.qm.imports.iQSL;

import com.qcadoo.mes.basic.imports.dtos.CellBinderRegistry;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

import static com.qcadoo.mes.basic.imports.dtos.CellBinder.required;
import static com.qcadoo.mes.basic.imports.dtos.CellBinder.optional;

@Component
public class IQSLCellBinderRegistry {
    private CellBinderRegistry cellBinderRegistry = new CellBinderRegistry();

    public IQSLCellBinderRegistry() {
    }

    @PostConstruct
    private void init() {
        cellBinderRegistry.setCellBinder(required("qualityStandardH"));
        cellBinderRegistry.setCellBinder(required("qualityCriteria"));
        cellBinderRegistry.setCellBinder(required("measuringEquipment"));
        cellBinderRegistry.setCellBinder(required("sampleSize"));
        cellBinderRegistry.setCellBinder(optional("position"));
        cellBinderRegistry.setCellBinder(optional("description"));
        cellBinderRegistry.setCellBinder(optional("qualitativeValue"));
        cellBinderRegistry.setCellBinder(optional("quantitativeValue"));
        cellBinderRegistry.setCellBinder(optional("tolerance"));
        cellBinderRegistry.setCellBinder(optional("upValue"));
        cellBinderRegistry.setCellBinder(optional("downValue"));
        cellBinderRegistry.setCellBinder(optional("unit"));
    }

    public CellBinderRegistry getCellBinderRegistry() {
        return this.cellBinderRegistry;
    }
}

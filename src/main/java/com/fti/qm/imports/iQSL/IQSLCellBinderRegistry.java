package com.fti.qm.imports.iQSL;

import com.qcadoo.mes.basic.imports.dtos.CellBinderRegistry;
import com.qcadoo.mes.basic.imports.helpers.CellParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

import static com.qcadoo.mes.basic.imports.dtos.CellBinder.required;
import static com.qcadoo.mes.basic.imports.dtos.CellBinder.optional;

@Component
public class IQSLCellBinderRegistry {
    private CellBinderRegistry cellBinderRegistry = new CellBinderRegistry();

    public IQSLCellBinderRegistry() {
    }

    @Autowired
    @Qualifier("qshIncomingByProductNumberParser")
    private CellParser qshIncomingLookupByProductNumberParser;

    @Autowired
    private CellParser qualityCriteriaCellParser;

    @Autowired
    private CellParser measuringEquipmentCellParser;

    @Autowired
    @Qualifier("qslQualitativeValueCellParser")
    private CellParser qSLQualitativeValueCellParser;

    @Autowired
    private CellParser decimalCommaToDotParser;

    @PostConstruct
    private void init() {
        cellBinderRegistry.setCellBinder(required("qualityStandardH", qshIncomingLookupByProductNumberParser));
        cellBinderRegistry.setCellBinder(required("qualityCriteria", qualityCriteriaCellParser));
        cellBinderRegistry.setCellBinder(required("measuringEquipment", measuringEquipmentCellParser));
        cellBinderRegistry.setCellBinder(required("sampleSize"));
        cellBinderRegistry.setCellBinder(optional("position"));
        cellBinderRegistry.setCellBinder(optional("description"));
        cellBinderRegistry.setCellBinder(optional("qualitativeValue", qSLQualitativeValueCellParser));
        cellBinderRegistry.setCellBinder(optional("quantitativeValue", decimalCommaToDotParser));
        cellBinderRegistry.setCellBinder(optional("tolerance", decimalCommaToDotParser));
        cellBinderRegistry.setCellBinder(optional("upValue", decimalCommaToDotParser));
        cellBinderRegistry.setCellBinder(optional("downValue", decimalCommaToDotParser));
        cellBinderRegistry.setCellBinder(optional("unit"));
    }

    public CellBinderRegistry getCellBinderRegistry() {
        return this.cellBinderRegistry;
    }
}

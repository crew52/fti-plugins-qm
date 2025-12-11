package com.fti.qm.imports.eQSL;

import com.fti.qm.constants.QSLFields;
import com.qcadoo.mes.basic.imports.dtos.CellBinderRegistry;
import com.qcadoo.mes.basic.imports.helpers.CellParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

import static com.qcadoo.mes.basic.imports.dtos.CellBinder.optional;
import static com.qcadoo.mes.basic.imports.dtos.CellBinder.required;

@Component
public class EQSLCellBinderRegistry {
    private CellBinderRegistry cellBinderRegistry = new CellBinderRegistry();

    public EQSLCellBinderRegistry() {
    }

    @Autowired
    private CellParser qualityCriteriaCellParser;

    @Autowired
    private CellParser measuringEquipmentCellParser;

    @Autowired
    @Qualifier("qslQualitativeValueCellParser")
    private CellParser qSLQualitativeValueCellParser;

    @Autowired
    private CellParser decimalCommaToDotParser;

    @Autowired
    @Qualifier("eqshInspectionTypeCellParser")
    private CellParser eqshInspectionTypeCellParser;

    @PostConstruct
    private void init() {
        cellBinderRegistry.setCellBinder(required("importToolNumber"));
        cellBinderRegistry.setCellBinder(required("importInspectionType", eqshInspectionTypeCellParser));
        cellBinderRegistry.setCellBinder(required(QSLFields.QUALITY_CRITERIA, qualityCriteriaCellParser));
        cellBinderRegistry.setCellBinder(required(QSLFields.MEASURING_EQUIPMENT, measuringEquipmentCellParser));
        cellBinderRegistry.setCellBinder(required(QSLFields.SAMPLE_SIZE));

        cellBinderRegistry.setCellBinder(optional(QSLFields.POSITION));
        cellBinderRegistry.setCellBinder(optional(QSLFields.DESCRIPTION));
        cellBinderRegistry.setCellBinder(optional(QSLFields.QUALITATIVE_VALUE, qSLQualitativeValueCellParser));
        cellBinderRegistry.setCellBinder(optional(QSLFields.QUANTITATIVE_VALUE, decimalCommaToDotParser));
        cellBinderRegistry.setCellBinder(optional(QSLFields.TOLERANCE, decimalCommaToDotParser));
        cellBinderRegistry.setCellBinder(optional(QSLFields.UP_VALUE, decimalCommaToDotParser));
        cellBinderRegistry.setCellBinder(optional(QSLFields.DOWN_VALUE, decimalCommaToDotParser));
        cellBinderRegistry.setCellBinder(optional(QSLFields.UNIT));
    }

    public CellBinderRegistry getCellBinderRegistry() {
        return this.cellBinderRegistry;
    }
}

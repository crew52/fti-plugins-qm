package com.fti.qm.imports.qualityCriteria;

import com.fti.qm.constants.QualityCriteriaFields;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.qcadoo.mes.basic.imports.dtos.CellBinderRegistry;
import com.qcadoo.mes.basic.imports.helpers.CellParser;

import static com.qcadoo.mes.basic.imports.dtos.CellBinder.optional;
import static com.qcadoo.mes.basic.imports.dtos.CellBinder.required;
import javax.annotation.PostConstruct;

@Component
public class QualityCriteriaCellBinderRegistry {
    private CellBinderRegistry cellBinderRegistry = new CellBinderRegistry();

    @Autowired
    private CellParser qualityCriteriaTypeCellParser;
    @PostConstruct
    private void init() {
        cellBinderRegistry.setCellBinder(required(QualityCriteriaFields.NUMBER));
        cellBinderRegistry.setCellBinder(required(QualityCriteriaFields.NAME));
        cellBinderRegistry.setCellBinder(optional(QualityCriteriaFields.TYPE, qualityCriteriaTypeCellParser));
        cellBinderRegistry.setCellBinder(optional(QualityCriteriaFields.UNIT));
    }

    public CellBinderRegistry getCellBinderRegistry() {
        return cellBinderRegistry;
    }
}

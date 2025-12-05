package com.fti.qm.imports.parsers;

import com.fti.qm.constants.MeasuringEquipmentFields;
import com.fti.qm.constants.QMConstants;
import com.qcadoo.mes.basic.imports.helpers.CellErrorsAccessor;
import com.qcadoo.mes.basic.imports.helpers.CellParser;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.function.Consumer;

@Component
public class MeasuringEquipmentCellParser implements CellParser {
    private static final String L_QCADOO_VIEW_VALIDATE_FIELD_ERROR_LOOKUP_CODE_NOT_FOUND = "qcadooView.validate.field.error.lookupCodeNotFound";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Override
    public void parse(final String cellValue, final String dependentCellValue, final CellErrorsAccessor errorsAccessor,
                      final Consumer<Object> valueConsumer) {
        Entity measuringEquipment = getMeasuringEquipmentByNumber(cellValue);

        if (Objects.isNull(measuringEquipment)) {
            errorsAccessor.addError(L_QCADOO_VIEW_VALIDATE_FIELD_ERROR_LOOKUP_CODE_NOT_FOUND);
        } else {
            valueConsumer.accept(measuringEquipment);
        }
    }

    private Entity getMeasuringEquipmentByNumber(final String number) {
        return getMeasuringEquipmentDD().find().add(SearchRestrictions.eq(MeasuringEquipmentFields.NUMBER, number)).setMaxResults(1)
                .uniqueResult();
    }

    private DataDefinition getMeasuringEquipmentDD() {
        return dataDefinitionService.get(QMConstants.PLUGIN_IDENTIFIER, QMConstants.MODEL_MEASURING_EQUIPMENT);
    }
}

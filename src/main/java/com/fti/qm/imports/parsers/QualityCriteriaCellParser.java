package com.fti.qm.imports.parsers;

import java.util.Objects;
import java.util.function.Consumer;

import com.fti.qm.constants.QMConstants;
import com.fti.qm.constants.QualityCriteriaFields;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.qcadoo.mes.basic.imports.helpers.CellErrorsAccessor;
import com.qcadoo.mes.basic.imports.helpers.CellParser;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;

@Component
public class QualityCriteriaCellParser implements CellParser{
    private static final String L_QCADOO_VIEW_VALIDATE_FIELD_ERROR_LOOKUP_CODE_NOT_FOUND = "qcadooView.validate.field.error.lookupCodeNotFound";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Override
    public void parse(final String cellValue, final String dependentCellValue, final CellErrorsAccessor errorsAccessor,
                      final Consumer<Object> valueConsumer) {
        Entity qualityCriteria = getQualityCriteriaByNumber(cellValue);

        if (Objects.isNull(qualityCriteria)) {
            errorsAccessor.addError(L_QCADOO_VIEW_VALIDATE_FIELD_ERROR_LOOKUP_CODE_NOT_FOUND);
        } else {
            valueConsumer.accept(qualityCriteria);
        }
    }

    private Entity getQualityCriteriaByNumber(final String number) {
        return getQualityCriteriaDD().find().add(SearchRestrictions.eq(QualityCriteriaFields.NUMBER, number)).setMaxResults(1)
                .uniqueResult();
    }

    private DataDefinition getQualityCriteriaDD() {
        return dataDefinitionService.get(QMConstants.PLUGIN_IDENTIFIER, QMConstants.MODEL_QUALITY_CRITERIA);
    }
}

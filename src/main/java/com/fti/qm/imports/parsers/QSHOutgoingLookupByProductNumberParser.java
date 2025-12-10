package com.fti.qm.imports.parsers;

import com.fti.qm.constants.QMConstants;
import com.fti.qm.constants.QSHFields;
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

@Component("qshOutgoingByProductNumberParser")
public class QSHOutgoingLookupByProductNumberParser implements CellParser{
    private static final String L_QCADOO_VIEW_VALIDATE_FIELD_ERROR_LOOKUP_CODE_NOT_FOUND = "qcadooView.validate.field.error.lookupCodeNotFound";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Override
    public void parse(final String cellValue, final String dependentCellValue, final CellErrorsAccessor errorsAccessor, final Consumer<Object> valueConsumer) {
        Entity qshOutgoing = findActiveQSHByProductNumber(cellValue);

        if (Objects.isNull(qshOutgoing)) {
            errorsAccessor.addError(L_QCADOO_VIEW_VALIDATE_FIELD_ERROR_LOOKUP_CODE_NOT_FOUND);
        } else {
            valueConsumer.accept(qshOutgoing);
        }
    }

    private Entity findActiveQSHByProductNumber(final String productNumber) {
        if (productNumber == null || productNumber.trim().isEmpty()) {
            return null;
        }

        return getQualityStandardHDD().find()
                .createAlias(QSHFields.PRODUCT, "p")
                .add(SearchRestrictions.eq("p.number", productNumber))
                .add(SearchRestrictions.eq(QSHFields.TYPE, QSHFields.Type.OUTGOING))
                .add(SearchRestrictions.eq(QSHFields.ACTIVE, true))
                .add(SearchRestrictions.eq(QSHFields.DELETED, false))
                .setMaxResults(1)
                .uniqueResult();
    }

    private DataDefinition getQualityStandardHDD() {
        return dataDefinitionService.get(QMConstants.PLUGIN_IDENTIFIER, QMConstants.MODEL_QUALITY_STANDARD_H);
    }
}

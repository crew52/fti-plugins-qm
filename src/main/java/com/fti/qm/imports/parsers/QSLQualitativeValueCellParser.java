package com.fti.qm.imports.parsers;

import com.fti.qm.constants.QSLFields;
import com.qcadoo.mes.basic.imports.helpers.CellErrorsAccessor;
import com.qcadoo.mes.basic.imports.helpers.CellParser;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.function.Consumer;

@Component("qslQualitativeValueCellParser")
public class QSLQualitativeValueCellParser implements CellParser {

    private static final String L_QCADOO_VIEW_VALIDATE_FIELD_ERROR_CUSTOM =
            "qcadooView.validate.field.error.custom";

    @Override
    public void parse(final String cellValue, final String dependentCellValue,
                      final CellErrorsAccessor errorsAccessor,
                      final Consumer<Object> valueConsumer) {

        if (cellValue == null || cellValue.trim().isEmpty()) {
            valueConsumer.accept(null);
            return;
        }

        String normalized = cellValue.trim().toLowerCase(Locale.ROOT);

        switch (normalized) {
            case "đạt":
            case "pass":
                valueConsumer.accept(QSLFields.QualitativeValue.PASS);
                break;
            default:
                errorsAccessor.addError(L_QCADOO_VIEW_VALIDATE_FIELD_ERROR_CUSTOM);
        }
    }
}

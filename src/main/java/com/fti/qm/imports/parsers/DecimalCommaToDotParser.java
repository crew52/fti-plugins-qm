package com.fti.qm.imports.parsers;

import com.qcadoo.mes.basic.imports.helpers.CellErrorsAccessor;
import com.qcadoo.mes.basic.imports.helpers.CellParser;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.function.Consumer;

@Component
public class DecimalCommaToDotParser implements CellParser {
    private static final String ERROR = "qcadooView.validate.field.error.invalidNumeric";

    @Override
    public void parse(String cellValue, String dependentCellValue,
                      CellErrorsAccessor errorsAccessor, Consumer<Object> valueConsumer) {

        if (cellValue == null || cellValue.trim().isEmpty()) {
            valueConsumer.accept(null);
            return;
        }

        String normalized = cellValue.trim().replace(",", ".");

        try {
            BigDecimal val = new BigDecimal(normalized);
            valueConsumer.accept(val);
        } catch (NumberFormatException ex) {
            errorsAccessor.addError(ERROR);
        }
    }
}

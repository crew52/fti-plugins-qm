package com.fti.qm.imports.parsers;

import java.util.Locale;
import java.util.Objects;
import java.util.function.Consumer;

import org.springframework.stereotype.Component;

import com.qcadoo.mes.basic.imports.helpers.CellErrorsAccessor;
import com.qcadoo.mes.basic.imports.helpers.CellParser;

@Component
public class QualityCriteriaTypeCellParser implements CellParser {

    private static final String L_QCADOO_VIEW_VALIDATE_FIELD_ERROR_CUSTOM =
            "qcadooView.validate.field.error.custom";

    @Override
    public void parse(final String cellValue, final String dependentCellValue,
                      final CellErrorsAccessor errorsAccessor,
                      final Consumer<Object> valueConsumer) {

        if (cellValue == null || cellValue.trim().isEmpty()) {
            // Trường type có thể để trống
            valueConsumer.accept(null);
            return;
        }

        String normalized = cellValue.trim().toLowerCase(Locale.ROOT);

        switch (normalized) {
            case "định tính":
            case "qualitative":
                valueConsumer.accept("01qualitative");
                break;

            case "định lượng":
            case "quantitative":
                valueConsumer.accept("02quantitative");
                break;

            default:
                // Nếu không khớp 4 giá trị hợp lệ => báo lỗi
                errorsAccessor.addError(L_QCADOO_VIEW_VALIDATE_FIELD_ERROR_CUSTOM);
        }
    }
}

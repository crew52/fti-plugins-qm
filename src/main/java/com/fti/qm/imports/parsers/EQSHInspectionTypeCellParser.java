package com.fti.qm.imports.parsers;

import com.fti.qm.constants.QSHFields;
import com.qcadoo.mes.basic.imports.helpers.CellErrorsAccessor;
import com.qcadoo.mes.basic.imports.helpers.CellParser;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.function.Consumer;

@Component("eqshInspectionTypeCellParser")
public class EQSHInspectionTypeCellParser implements CellParser {
    private static final String L_QCADOO_VIEW_VALIDATE_FIELD_ERROR_CUSTOM =
            "qcadooView.validate.field.error.custom";

    public static final String VI_EQUIPMENT = "thiết bị";
    public static final String EN_EQUIPMENT = "equipment";

    public static final String VI_INTERNAL = "hiệu chuẩn - trong";
    public static final String EN_INTERNAL = "internal calibration";

    public static final String VI_EXTERNAL = "hiệu chuẩn - ngoài";
    public static final String EN_EXTERNAL = "external calibration";

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
            case VI_EQUIPMENT:
            case EN_EQUIPMENT:
                valueConsumer.accept(QSHFields.InspectionType.EQUIPMENT);
                break;

            case VI_INTERNAL:
            case EN_INTERNAL:
                valueConsumer.accept(QSHFields.InspectionType.INTERNAL_CALIBRATION);
                break;

            case VI_EXTERNAL:
            case EN_EXTERNAL:
                valueConsumer.accept(QSHFields.InspectionType.EXTERNAL_CALIBRATION);
                break;

            default:
                errorsAccessor.addError(L_QCADOO_VIEW_VALIDATE_FIELD_ERROR_CUSTOM);
        }
    }
}

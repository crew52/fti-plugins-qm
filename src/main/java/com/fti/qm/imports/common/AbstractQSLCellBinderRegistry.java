package com.fti.qm.imports.common;

import com.fti.qm.constants.QSLFields;
import com.qcadoo.mes.basic.imports.dtos.CellBinderRegistry;
import com.qcadoo.mes.basic.imports.helpers.CellParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.annotation.PostConstruct;

import static com.qcadoo.mes.basic.imports.dtos.CellBinder.required;
import static com.qcadoo.mes.basic.imports.dtos.CellBinder.optional;


/**
 * Lớp abstract cấu hình CellBinderRegistry dùng cho import QSL.
 * Gom logic map cột Excel vào entity QSL.
 *
 * Subclass chỉ cần override getLookupParser() để cung cấp parser lookup
 * sản phẩm tương ứng (Incoming/Outgoing/...).
 */
public abstract class AbstractQSLCellBinderRegistry {
    protected final CellBinderRegistry cellBinderRegistry = new CellBinderRegistry();

    // Các parser dùng chung
    @Autowired protected CellParser qualityCriteriaCellParser;
    @Autowired protected CellParser measuringEquipmentCellParser;
    @Autowired @Qualifier("qslQualitativeValueCellParser")
    protected CellParser qSLQualitativeValueCellParser;
    @Autowired protected CellParser decimalCommaToDotParser;

    /**
     * Parser lookup sản phẩm — subclass phải override.
     */
    protected abstract CellParser getLookupParser();

    /**
     * Đăng ký các cell binder cho các trường QSL.
     * Chạy tự động sau khi Spring inject dependencies.
     */
    @PostConstruct
    protected void init() {
        cellBinderRegistry.setCellBinder(required(QSLFields.QUALITY_STANDARD_H, getLookupParser()));
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

    /**
     * Trả về registry đã được cấu hình.
     */
    public CellBinderRegistry getCellBinderRegistry() {
        return this.cellBinderRegistry;
    }
}

package com.fti.qm.imports.parsers;

import com.qcadoo.mes.basic.imports.helpers.CellParser;
import com.fti.qm.constants.QMConstants;
import com.fti.qm.constants.QSHFields;
import com.qcadoo.mes.basic.imports.helpers.CellErrorsAccessor;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Parser trừu tượng dùng để tìm bản ghi Quality Standard H (QSH)
 * theo mã sản phẩm khi import file XLSX.
 *
 * <p>Class này gom toàn bộ logic tìm kiếm chung cho các loại QSH:
 * INCOMING, OUTGOING, IN_PROCESS... Lớp con chỉ cần triển khai
 * phương thức {@link #getType()} để trả về loại QSH cần tìm.</p>
 *
 * <p>Luồng xử lý:</p>
 * <ol>
 *     <li>Nhận mã sản phẩm từ ô Excel.</li>
 *     <li>Tìm QSH theo:
 *         <ul>
 *             <li>product.number</li>
 *             <li>type (từ lớp con)</li>
 *             <li>active = true</li>
 *             <li>deleted = false</li>
 *         </ul>
 *     </li>
 *     <li>Nếu tìm thấy → trả về entity.</li>
 *     <li>Nếu không → thêm lỗi lookup không tồn tại.</li>
 * </ol>
 */
public abstract class AbstractQSHLookupByProductNumberParser implements CellParser {
    private static final String L_QCADOO_VIEW_VALIDATE_FIELD_ERROR_LOOKUP_CODE_NOT_FOUND =
            "qcadooView.validate.field.error.lookupCodeNotFound";

    @Autowired
    protected DataDefinitionService dataDefinitionService;

    /**
     * Lớp con trả về QSHFields.Type cần tìm
     * (INCOMING, OUTGOING hoặc IN_PROCESS).
     *
     * @return type của QSH
     */
    protected abstract String getType();

    @Override
    public void parse(final String cellValue, final String dependentCellValue,
                      final CellErrorsAccessor errorsAccessor,
                      final Consumer<Object> valueConsumer) {

        Entity qsh = findActiveQSHByProductNumber(cellValue);

        if (Objects.isNull(qsh)) {
            errorsAccessor.addError(L_QCADOO_VIEW_VALIDATE_FIELD_ERROR_LOOKUP_CODE_NOT_FOUND);
        } else {
            valueConsumer.accept(qsh);
        }
    }

    /**
     * Tìm bản ghi QSH theo mã sản phẩm + loại QSH.
     *
     * @param productNumber mã sản phẩm
     * @return entity QSH nếu tồn tại; null nếu không
     */
    private Entity findActiveQSHByProductNumber(final String productNumber) {
        if (productNumber == null || productNumber.trim().isEmpty()) {
            return null;
        }

        return getQualityStandardHDD().find()
                .createAlias(QSHFields.PRODUCT, "p")
                .add(SearchRestrictions.eq("p.number", productNumber))
                .add(SearchRestrictions.eq(QSHFields.TYPE, getType()))
                .add(SearchRestrictions.eq(QSHFields.ACTIVE, true))
                .add(SearchRestrictions.eq(QSHFields.DELETED, false))
                .setMaxResults(1)
                .uniqueResult();
    }

    /**
     * Lấy DataDefinition của bảng qualityStandardH.
     */
    private DataDefinition getQualityStandardHDD() {
        return dataDefinitionService.get(
                QMConstants.PLUGIN_IDENTIFIER,
                QMConstants.MODEL_QUALITY_STANDARD_H
        );
    }
}

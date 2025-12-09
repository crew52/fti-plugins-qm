package com.fti.qm.hooks;

import com.fti.qm.constants.QMConstants;
import com.fti.qm.constants.QualityCriteriaFields;
import com.fti.qm.helpers.SoftDeleteHelper;
import com.qcadoo.model.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Hooks cho QualityCriteria, kiểm tra ràng buộc khi xóa mềm.
 */
@Service
public class QualityCriteriaHooks {

    @Autowired
    private SoftDeleteHelper softDeleteHelper;

    /**
     * Kiểm tra trước khi cập nhật.
     * Nếu bản ghi bị đánh dấu xóa, xác minh xem có bản ghi QualityStandardL đang sử dụng nó hay không.
     *
     * @param dataDefinition  Định nghĩa dữ liệu QualityCriteria
     * @param qualityCriteria Bản ghi đang cập nhật
     */
    public void onUpdate(final DataDefinition dataDefinition, final Entity qualityCriteria) {
        softDeleteHelper.validateSoftDelete(
                dataDefinition,
                qualityCriteria,
                QMConstants.PLUGIN_IDENTIFIER,
                QMConstants.MODEL_QUALITY_STANDARD_L,
                QualityCriteriaFields.QUALITY_CRITERIA_ID,
                "qm.qualityCriteria.deleteBlocked"
        );
    }
}
package com.fti.qm.hooks;

import com.fti.qm.constants.GlobalFields;
import com.fti.qm.constants.MeasuringEquipmentFields;
import com.fti.qm.constants.QMConstants;
import com.fti.qm.helpers.SoftDeleteHelper;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.utils.NumberGeneratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Hooks cho model MeasuringEquipment.
 *
 * <p>Phụ trách sinh số tự động khi tạo mới và kiểm tra ràng buộc
 * khi thực hiện thao tác xóa mềm (soft delete).</p>
 */
@Service
public class MeasuringEquipmentHooks {

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    @Autowired
    private SoftDeleteHelper softDeleteHelper;

    /**
     * Hook chạy trước khi tạo mới bản ghi.
     *
     * <p>Nếu trường 'number' để trống, hệ thống sẽ tự sinh mã đo lường mới.
     * Việc sinh mã diễn ra trước bước validate nhằm đảm bảo mã sinh ra
     * vẫn được kiểm tra hợp lệ bởi hệ thống.</p>
     *
     * @param dd     DataDefinition của model MeasuringEquipment
     * @param entity Bản ghi đang được tạo
     */
    public void onCreate(final DataDefinition dd, final Entity entity) {
        // Sinh tự động khi record được tạo (trước khi validate)
        String number = entity.getStringField(MeasuringEquipmentFields.NUMBER);
        if (number == null || number.trim().isEmpty()) {
            String generated = numberGeneratorService.generateNumber(
                    QMConstants.PLUGIN_IDENTIFIER,
                    QMConstants.MODEL_MEASURING_EQUIPMENT
            );
            entity.setField(MeasuringEquipmentFields.NUMBER, generated);
        }
    }

    /**
     * Kiểm tra trước khi cập nhật.
     * Nếu bản ghi bị đánh dấu xóa, xác minh xem có bản ghi QualityStandardL đang sử dụng nó hay không.
     *
     * @param dataDefinition  Định nghĩa dữ liệu MeasuringEquipment
     * @param measuringEquipment Bản ghi đang cập nhật
     */
    public void onUpdate(final DataDefinition dataDefinition, final Entity measuringEquipment) {
        softDeleteHelper.validateSoftDelete(
                dataDefinition,
                measuringEquipment,
                QMConstants.PLUGIN_IDENTIFIER,
                QMConstants.MODEL_QUALITY_STANDARD_L,
                MeasuringEquipmentFields.MEASURING_EQUIPMENT_ID,
                "qm.measuringEquipment.deleteBlocked"
        );
    }
}

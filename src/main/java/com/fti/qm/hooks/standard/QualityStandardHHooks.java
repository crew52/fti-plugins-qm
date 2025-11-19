package com.fti.qm.hooks.standard;

import com.fti.qm.constants.QSHFields;
import com.fti.qm.hooks.standard.base.BaseQualityStandardHooksRe;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class QualityStandardHHooks extends BaseQualityStandardHooksRe {

    public static final String PRODUCT_ACTIVE_TYPE01 = "qm.message.error.productAlreadyExistsWithActiveStandardType01";
    public static final String PRODUCT_ACTIVE_TYPE02 = "qm.message.error.productAlreadyExistsWithActiveStandardType02";
    public static final String PRODUCT_ACTIVE_TYPE03 = "qm.message.error.productAlreadyExistsWithActiveStandardType03";
    public static final String PRODUCT_ACTIVE_DEFAULT = "qm.message.error.productAlreadyExistsWithActiveStandard";
    public static final String TOOL_ACTIVE_TYPE04 = "qm.message.error.toolAndTypeAlreadyExistsWithActiveStandardType04";
    @Autowired
    public QualityStandardHHooks(DataDefinitionService dataDefinitionService) {
        super(dataDefinitionService);
    }

    public void onSave(final DataDefinition dd, final Entity entity) {
        String typeValue = entity.getStringField(QSHFields.TYPE);
        if (typeValue == null) {
            return;
        }

        switch (typeValue) {
            case QSHFields.Type.INCOMING:
            case QSHFields.Type.INPROCESS:
            case QSHFields.Type.OUTGOING:
                validateUniqueActiveProduct(dd, entity);
                break;

            case QSHFields.Type.EQUIPMENT:
                validateUniqueActiveToolInspectionType(dd, entity);
                break;

            default:
                // type không xác định → không validate
                break;
        }
    }

    /**
     * Validate khi type là 01, 02, 03 → sử dụng Product
     */
    public void validateUniqueActiveProduct(final DataDefinition dd, final Entity entity) {
        String typeValue = entity.getStringField(QSHFields.TYPE);
        if (typeValue == null) return;

        String messageKey;
        switch (typeValue) {
            case QSHFields.Type.INCOMING:
                messageKey = PRODUCT_ACTIVE_TYPE01;
                break;
            case QSHFields.Type.INPROCESS:
                messageKey = PRODUCT_ACTIVE_TYPE02;
                break;
            case QSHFields.Type.OUTGOING:
                messageKey = PRODUCT_ACTIVE_TYPE03;
                break;
            default:
                messageKey = PRODUCT_ACTIVE_DEFAULT;
                break;
        }

        // Dùng helper trong BaseQualityStandardHooksRe
        validateUniqueActiveCombination(dd, entity,
                QSHFields.PRODUCT,
                QSHFields.PRODUCT_ID,
                QSHFields.ACTIVE,
                typeValue,
                messageKey);
    }

    /**
     * Validate khi type là 04equipment → sử dụng tool + inspectionType
     */
    public void validateUniqueActiveToolInspectionType(final DataDefinition dd, final Entity entity) {
        super.validateUniqueActiveToolInspectionType(dd, entity,
                QSHFields.TOOL,
                QSHFields.TOOL_ID,
                QSHFields.INSPECTION_TYPE,
                QSHFields.ACTIVE,
                TOOL_ACTIVE_TYPE04);
    }

    public void setStatusText(final ViewDefinitionState view) {
        handleSetStatusText(view,
                QSHFields.QUALITY_STANDARD_LS,
                QSHFields.STATUS_TEXT,
                QSHFields.StatusText.NO_STANDARD,
                QSHFields.StatusText.HAS_STANDARD);
    }
}

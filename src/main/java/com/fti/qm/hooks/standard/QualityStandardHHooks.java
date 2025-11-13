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
    @Autowired
    public QualityStandardHHooks(DataDefinitionService dataDefinitionService) {
        super(dataDefinitionService);
    }

    public void validateUniqueActiveProduct(final DataDefinition dd, final Entity entity) {
        String typeValue = entity.getStringField(QSHFields.TYPE);
        if (typeValue == null) return;

        String messageKey;
        switch (typeValue) {
            case QSHFields.Type.INCOMING:
                messageKey = "qm.message.error.productAlreadyExistsWithActiveStandardType01";
                break;
            case QSHFields.Type.INPROCESS:
                messageKey = "qm.message.error.productAlreadyExistsWithActiveStandardType02";
                break;
            case QSHFields.Type.OUTGOING:
                messageKey = "qm.message.error.productAlreadyExistsWithActiveStandardType03";
                break;
            case QSHFields.Type.EQUIPMENT:
                messageKey = "qm.message.error.productAlreadyExistsWithActiveStandardType04";
                break;
            default:
                messageKey = "qm.message.error.productAlreadyExistsWithActiveStandard";
        }

        validateUniqueActiveCombination(dd, entity,
                QSHFields.PRODUCT,
                QSHFields.PRODUCT_ID,
                QSHFields.ACTIVE,
                typeValue,
                messageKey);
    }

    public void setStatusText(final ViewDefinitionState view) {
        handleSetStatusText(view,
                QSHFields.QUALITY_STANDARD_LS,
                QSHFields.STATUS_TEXT,
                QSHFields.StatusText.NO_STANDARD,
                QSHFields.StatusText.HAS_STANDARD);
    }
}

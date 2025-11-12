package com.fti.qm.hooks.standard;

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
        String typeValue = entity.getStringField("type");
        if (typeValue == null) return;

        String messageKey;
        switch (typeValue) {
            case "01incoming":
                messageKey = "qm.message.error.productAlreadyExistsWithActiveStandardType01";
                break;
            case "02inprocess":
                messageKey = "qm.message.error.productAlreadyExistsWithActiveStandardType02";
                break;
            case "03outgoing":
                messageKey = "qm.message.error.productAlreadyExistsWithActiveStandardType03";
                break;
            case "04equipment":
                messageKey = "qm.message.error.productAlreadyExistsWithActiveStandardType04";
                break;
            default:
                messageKey = "qm.message.error.productAlreadyExistsWithActiveStandard";
        }

        validateUniqueActiveCombination(dd, entity,
                "product",
                "product.id",
                "active",
                typeValue,
                messageKey);
    }

    public void setStatusText(final ViewDefinitionState view) {
        handleSetStatusText(view,
                "qualityStandardLs",
                "statusText",
                "01noStandard",
                "02hasStandard");
    }
}

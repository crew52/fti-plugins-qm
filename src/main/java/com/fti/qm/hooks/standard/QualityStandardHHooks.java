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

        validateUniqueActiveCombination(dd, entity,
                "product",
                "product.id",
                "active",
                typeValue,
                "qm.message.error.productAlreadyExistsWithActiveStandard");
    }

    public void setStatusText(final ViewDefinitionState view) {
        handleSetStatusText(view,
                "qualityStandardLs",
                "statusText",
                "01noStandard",
                "02hasStandard");
    }
}

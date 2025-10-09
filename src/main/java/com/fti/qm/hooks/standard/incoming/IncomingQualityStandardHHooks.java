package com.fti.qm.hooks.standard.incoming;

import com.fti.qm.constants.IncomingQualityStandardHFields;
import com.fti.qm.hooks.standard.base.BaseQualityStandardHooks;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class IncomingQualityStandardHHooks extends BaseQualityStandardHooks {

    @Autowired
    public IncomingQualityStandardHHooks(DataDefinitionService dataDefinitionService) {
        super(dataDefinitionService);
    }

    public void onSave(final DataDefinition dd, final Entity entity) {
        handleOnSave(dd, entity,
                IncomingQualityStandardHFields.PRODUCT,
                IncomingQualityStandardHFields.PRODUCT_ID,
                IncomingQualityStandardHFields.ACTIVE,
                "qm.message.error.productAlreadyExistsWithActiveStandard");
    }

    public void setStatusText(final ViewDefinitionState view) {
        handleSetStatusText(view,
                IncomingQualityStandardHFields.INCOMING_QUALITY_STANDARD_LS,
                IncomingQualityStandardHFields.STATUS_TEXT,
                IncomingQualityStandardHFields.STATUS_NO_STANDARD,
                IncomingQualityStandardHFields.STATUS_HAS_STANDARD);
    }
}

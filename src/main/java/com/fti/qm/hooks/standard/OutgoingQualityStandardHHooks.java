package com.fti.qm.hooks.standard;

import com.fti.qm.constants.outgoingQualityStandard.OutgoingQualityStandardHFields;
import com.fti.qm.hooks.standard.base.BaseQualityStandardHooks;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OutgoingQualityStandardHHooks extends BaseQualityStandardHooks {

    @Autowired
    public OutgoingQualityStandardHHooks(DataDefinitionService dataDefinitionService) {
        super(dataDefinitionService);
    }

    public void onSave(final DataDefinition dd, final Entity entity) {
        handleOnSave(dd, entity,
                OutgoingQualityStandardHFields.PRODUCT,
                OutgoingQualityStandardHFields.PRODUCT_ID,
                OutgoingQualityStandardHFields.ACTIVE,
                "qm.message.error.outgoing.productAlreadyExistsWithActiveStandard");
    }

    public void setStatusText(final ViewDefinitionState view) {
        handleSetStatusText(view,
                OutgoingQualityStandardHFields.OUTGOING_QUALITY_STANDARD_LS,
                OutgoingQualityStandardHFields.STATUS_TEXT,
                OutgoingQualityStandardHFields.STATUS_NO_STANDARD,
                OutgoingQualityStandardHFields.STATUS_HAS_STANDARD);
    }
}
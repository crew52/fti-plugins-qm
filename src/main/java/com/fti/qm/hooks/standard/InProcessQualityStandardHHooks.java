package com.fti.qm.hooks.standard;

import com.fti.qm.constants.inProcessQualityStandard.InProcessQualityStandardHFields;
import com.fti.qm.hooks.standard.base.BaseQualityStandardHooks;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InProcessQualityStandardHHooks extends BaseQualityStandardHooks {

    @Autowired
    public InProcessQualityStandardHHooks(DataDefinitionService dataDefinitionService) {
        super(dataDefinitionService);
    }

    public void onSave(final DataDefinition dd, final Entity entity) {
        handleOnSave(dd, entity,
                InProcessQualityStandardHFields.PRODUCT,
                InProcessQualityStandardHFields.PRODUCT_ID,
                InProcessQualityStandardHFields.ACTIVE,
                "qm.message.error.inProcess.productAlreadyExistsWithActiveStandard");
    }

    public void setStatusText(final ViewDefinitionState view) {
        handleSetStatusText(view,
                InProcessQualityStandardHFields.IN_PROCESS_QUALITY_STANDARD_LS,
                InProcessQualityStandardHFields.STATUS_TEXT,
                InProcessQualityStandardHFields.STATUS_NO_STANDARD,
                InProcessQualityStandardHFields.STATUS_HAS_STANDARD);
    }
}


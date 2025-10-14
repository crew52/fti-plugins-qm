package com.fti.qm.hooks.standard.equipment;

import com.fti.qm.hooks.standard.base.BaseQualityStandardHDetailsHooks;
import com.qcadoo.view.api.ViewDefinitionState;
import org.springframework.stereotype.Service;

@Service
public class EquipmentQualityStandardHDetailsHooks extends BaseQualityStandardHDetailsHooks {
    public void setEQSHIdForMultiUploadField(final ViewDefinitionState view) {
        setMultiUploadContext(view, "eQSHIdForMultiUpload", "eQSHMultiUploadLocale");
    }
}

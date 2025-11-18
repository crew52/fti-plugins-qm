package com.fti.qm.hooks.standard;

import com.fti.qm.hooks.standard.base.BaseQualityStandardHDetailsHooks;
import com.qcadoo.view.api.ViewDefinitionState;
import org.springframework.stereotype.Service;

@Service
public class QICDetailsHooks extends BaseQualityStandardHDetailsHooks {
    public void setQICIdForMultiUploadField(final ViewDefinitionState view) {
        setMultiUploadContext(view, "qicIdForMultiUpload", "qicMultiUploadLocale");
    }

}

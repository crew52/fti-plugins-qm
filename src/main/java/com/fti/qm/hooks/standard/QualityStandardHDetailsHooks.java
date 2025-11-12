package com.fti.qm.hooks.standard;

import com.fti.qm.hooks.standard.base.BaseQualityStandardHDetailsHooks;
import com.qcadoo.view.api.ViewDefinitionState;
import org.springframework.stereotype.Service;

@Service
public class QualityStandardHDetailsHooks extends BaseQualityStandardHDetailsHooks {
    public void setQSHIdForMultiUploadField(final ViewDefinitionState view) {
        setMultiUploadContext(view, "qSHIdForMultiUpload", "qSHMultiUploadLocale");
    }
}

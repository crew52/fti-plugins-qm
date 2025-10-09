package com.fti.qm.hooks.standard.inProcess;

import com.fti.qm.hooks.standard.base.BaseQualityStandardHDetailsHooks;
import com.qcadoo.view.api.ViewDefinitionState;
import org.springframework.stereotype.Service;

@Service
public class InProcessQualityStandardHDetailsHooks extends BaseQualityStandardHDetailsHooks {

    public void setIPQSHIdForMultiUploadField(final ViewDefinitionState view) {
        setMultiUploadContext(view, "iPQSHIdForMultiUpload", "iPQSHMultiUploadLocale");
    }
}


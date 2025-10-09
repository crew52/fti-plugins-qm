package com.fti.qm.hooks.standard;

import com.fti.qm.hooks.standard.base.BaseQualityStandardHDetailsHooks;
import com.qcadoo.view.api.ViewDefinitionState;
import org.springframework.stereotype.Service;

@Service
public class OutgoingQualityStandardHDetailsHooks extends BaseQualityStandardHDetailsHooks {

    public void setOQSHIdForMultiUploadField(final ViewDefinitionState view) {
        setMultiUploadContext(view, "oQSHIdForMultiUpload", "oQSHMultiUploadLocale");
    }
}


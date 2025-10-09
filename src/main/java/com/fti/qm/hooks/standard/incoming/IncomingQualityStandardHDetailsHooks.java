package com.fti.qm.hooks.standard.incoming;

import com.fti.qm.hooks.standard.base.BaseQualityStandardHDetailsHooks;
import com.qcadoo.view.api.ViewDefinitionState;
import org.springframework.stereotype.Service;

@Service
public class IncomingQualityStandardHDetailsHooks extends BaseQualityStandardHDetailsHooks {

    public void setIQSHIdForMultiUploadField(final ViewDefinitionState view) {
        setMultiUploadContext(view, "iQSHIdForMultiUpload", "iQSHMultiUploadLocale");
    }
}


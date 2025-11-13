package com.fti.qm.hooks.standard;

import com.fti.qm.constants.QSHFields;
import com.fti.qm.hooks.standard.base.BaseQualityStandardHDetailsHooks;
import com.qcadoo.view.api.ViewDefinitionState;
import org.springframework.stereotype.Service;

@Service
public class QualityStandardHDetailsHooks extends BaseQualityStandardHDetailsHooks {
    public void setQSHIdForMultiUploadField(final ViewDefinitionState view) {
        setMultiUploadContext(view, "qSHIdForMultiUpload", "qSHMultiUploadLocale");
    }

    public void setDefaultTypeValueIncoming(final ViewDefinitionState view) {
        setDefaultTypeValue(view, QSHFields.Type.INCOMING);
    }

    public void setDefaultTypeValueOutgoing(final ViewDefinitionState view) {
        setDefaultTypeValue(view, QSHFields.Type.OUTGOING);
    }
}

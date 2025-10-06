package com.fti.qm.hooks.outgoingQualityStandard;

import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

@Service
public class OutgoingQualityStandardHDetailsHooks {

    public void setOQSHIdForMultiUploadField(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        FieldComponent idField = (FieldComponent) view.getComponentByReference("oQSHIdForMultiUpload");
        FieldComponent localeField = (FieldComponent) view.getComponentByReference("oQSHMultiUploadLocale");

        if (form.getEntityId() != null) {
            idField.setFieldValue(form.getEntityId());
        } else {
            idField.setFieldValue("");
        }
        idField.requestComponentUpdateState();

        localeField.setFieldValue(LocaleContextHolder.getLocale());
        localeField.requestComponentUpdateState();
    }
}
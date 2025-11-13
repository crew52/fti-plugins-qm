package com.fti.qm.hooks.standard.base;

import com.fti.qm.constants.QSHFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.context.i18n.LocaleContextHolder;

public class BaseQualityStandardHDetailsHooks {
    protected void setMultiUploadContext(final ViewDefinitionState view, final String idFieldRef, final String localeFieldRef) {
        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        FieldComponent idField = (FieldComponent) view.getComponentByReference(idFieldRef);
        FieldComponent localeField = (FieldComponent) view.getComponentByReference(localeFieldRef);

        if (form.getEntityId() != null) {
            idField.setFieldValue(form.getEntityId());
        } else {
            idField.setFieldValue("");
        }
        idField.requestComponentUpdateState();

        localeField.setFieldValue(LocaleContextHolder.getLocale());
        localeField.requestComponentUpdateState();
    }

    protected void setDefaultTypeValue(final ViewDefinitionState view, final String typeValue) {
        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        if (form == null) {
            return;
        }

        Entity entity = form.getEntity();
        if (entity == null || entity.getId() != null) {
            // Chỉ set giá trị mặc định nếu là bản ghi mới
            return;
        }

        entity.setField(QSHFields.TYPE, typeValue);

        FieldComponent typeField = (FieldComponent) view.getComponentByReference(QSHFields.TYPE);
        if (typeField != null) {
            typeField.setFieldValue(typeValue);
            typeField.requestComponentUpdateState();
        }

        form.setEntity(entity);
    }
}

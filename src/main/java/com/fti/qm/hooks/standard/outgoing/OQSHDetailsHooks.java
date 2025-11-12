package com.fti.qm.hooks.standard.outgoing;

import org.springframework.stereotype.Service;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.FieldComponent;

@Service
public class OQSHDetailsHooks {
    public void setDefaultTypeValue(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference("form");

        if (form == null) {
            return;
        }

        Entity entity = form.getEntity();

        // Nếu là bản ghi mới (chưa có id) => set giá trị mặc định
        if (entity.getId() == null) {
            entity.setField("type", "03outgoing");

            // Cập nhật hiển thị trên view
            FieldComponent typeField = (FieldComponent) view.getComponentByReference("type");
            if (typeField != null) {
                typeField.setFieldValue("01incoming");
                typeField.requestComponentUpdateState();
            }

            form.setEntity(entity);
        }
    }
}

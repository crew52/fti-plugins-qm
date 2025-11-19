package com.fti.qm.listeners;

import com.fti.qm.constants.qualityInspectionCommand.QICFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class QICWarehouseTransferListeners {
    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void transferWarehouse(final ViewDefinitionState view, final ComponentState state, final String[] args) {

        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        Long id = form.getEntityId();

        // 👉 Load entity thật từ DB
        DataDefinition qicDD = dataDefinitionService.get("qm", "qualityInspectionCommandRe");
        Entity qic = qicDD.get(id);

        String status = qic.getStringField("status");

        // 👉 Chỉ cho phép status = 02inProcess
        if (!"02inProgress".equals(status)) {
            view.addMessage(
                    "qm.qic.transferWarehouse.invalidStatus", ComponentState.MessageType.FAILURE
            );
            return;
        }

        // 👉 Status hợp lệ -> xử lý tiếp
        view.addMessage(
                "qm.qic.transferWarehouse.validStatus", ComponentState.MessageType.SUCCESS
        );

        // TODO: Place your actual transferWarehouse logic here...
    }
}

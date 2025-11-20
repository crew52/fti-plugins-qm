package com.fti.qm.listeners;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.security.api.SecurityService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
public class QICWarehouseTransferListeners {
    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private SecurityService securityService;

    public void transferWarehouse(final ViewDefinitionState view, final ComponentState state, final String[] args) {

        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        Long id = form.getEntityId();

        if (id == null) {
            view.addMessage("qm.qic.transferWarehouse.noData", ComponentState.MessageType.FAILURE);
            return;
        }

        DataDefinition qicDD = dataDefinitionService.get("qm", "qualityInspectionCommandRe");
        Entity qic = qicDD.get(id);

        String status = qic.getStringField("status");

        // Chỉ cho phép status = 02inProgress
        if (!"02inProgress".equals(status)) {
            view.addMessage(
                    "qm.qic.transferWarehouse.invalidStatus", ComponentState.MessageType.FAILURE
            );
            return;
        }

        view.addMessage(
                "qm.qic.transferWarehouse.validStatus", ComponentState.MessageType.SUCCESS
        );

        // Tạo document dựa theo qualityDecision
        createDocumentsForQIC(qic);

        // Cập nhật status QIC thành completed
        updateQICStatusToCompleted(qic);

        // Set entity lại vào form
        form.setEntity(qic);
        view.addMessage("qm.qic.transferWarehouse.success", ComponentState.MessageType.SUCCESS);
    }

    private void createDocumentsForQIC(Entity qic) {

        DataDefinition documentDD = dataDefinitionService.get("materialFlowResources", "document");

        String decision = qic.getStringField("qualityDecision");
        Entity locationFrom = qic.getBelongsToField("location");
        Entity warehouseLocation = qic.getBelongsToField("warehouseLocation");
        Entity ngLocation = qic.getBelongsToField("ngLocation");

        switch (decision) {
            case "01accept":
                createSingleDocument(documentDD, qic, locationFrom, warehouseLocation);
                break;
            case "02reject":
                createSingleDocument(documentDD, qic, locationFrom, ngLocation);
                break;
            case "03partial":
                createSingleDocument(documentDD, qic, locationFrom, warehouseLocation);
                createSingleDocument(documentDD, qic, locationFrom, ngLocation);
                break;
            default:
                break;
        }
    }

    private void createSingleDocument(
            DataDefinition documentDD,
            Entity qic,
            Entity locationFrom,
            Entity locationTo
    ) {
        if (locationTo == null) {
            return;
        }

        try {
            Entity newDoc = documentDD.create();
            newDoc.setField("name", "Transfer from QIC " + qic.getStringField("poNumber"));
            newDoc.setField("type", "05transfer");
            newDoc.setField("time", java.util.Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()));
            newDoc.setField("locationFrom", locationFrom);
            newDoc.setField("locationTo", locationTo);
            newDoc.setField("company", qic.getBelongsToField("company"));
            newDoc.setField("user", securityService.getCurrentUserId());
            newDoc.setField("description", "Auto created from QIC transfer - PO " + qic.getStringField("poNumber"));
            newDoc.setField("state", "02accepted");

            newDoc = documentDD.save(newDoc); // Gán lại newDoc

            DataDefinition positionDD = dataDefinitionService.get("materialFlowResources", "position");

            // Tạo Position dựa trên QIC
            Entity newPosition = positionDD.create();
            newPosition.setField("document", newDoc);
            newPosition.setField("product", qic.getBelongsToField("product"));
            newPosition.setField("quantity", qic.getField("transactionQuantity"));

            newPosition = positionDD.save(newPosition);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateQICStatusToCompleted(Entity qic) {
        try {
            qic.setField("status", "03completed");
            DataDefinition qicDD = dataDefinitionService.get("qm", "qualityInspectionCommandRe");
            qicDD.save(qic);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

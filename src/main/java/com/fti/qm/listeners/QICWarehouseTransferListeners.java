package com.fti.qm.listeners;

import com.fti.qm.constants.QMConstants;
import com.fti.qm.constants.qualityInspectionCommand.QICFields;
import com.qcadoo.mes.materialFlowResources.constants.MaterialFlowResourcesConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchOrders;
import com.qcadoo.security.api.SecurityService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
public class QICWarehouseTransferListeners {
    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private SecurityService securityService;

    @Transactional
    public void transferWarehouse(final ViewDefinitionState view, final ComponentState state, final String[] args) {

        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        Long id = form.getEntityId();

        if (id == null) {
            view.addMessage("qm.qic.transferWarehouse.noData", ComponentState.MessageType.FAILURE);
            return;
        }

        DataDefinition qicDD = dataDefinitionService.get(QMConstants.PLUGIN_IDENTIFIER, QMConstants.MODEL_QUALITY_INSPECTION_COMMAND);
        Entity qic = qicDD.get(id);

        String status = qic.getStringField(QICFields.STATUS);

        // Chỉ cho phép status = 02inProgress
        if (!QICFields.Status.IN_PROGRESS.equals(status)) {
            view.addMessage(
                    "qm.qic.transferWarehouse.invalidStatus", ComponentState.MessageType.FAILURE
            );
            return;
        }
        // Tạo document dựa theo qualityDecision
        createDocumentsForQIC(qic);

        // Cập nhật status QIC thành completed
        updateQICStatusToCompleted(qic);

        // Set entity lại vào form
        form.setEntity(qic);
        view.addMessage("qm.qic.transferWarehouse.success", ComponentState.MessageType.SUCCESS);
    }

    private void createDocumentsForQIC(Entity qic) {

        DataDefinition documentDD = dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER, MaterialFlowResourcesConstants.MODEL_DOCUMENT);

        String decision = qic.getStringField(QICFields.QUALITY_DECISION);
        Entity locationFrom = qic.getBelongsToField(QICFields.LOCATION);
        Entity warehouseLocation = qic.getBelongsToField(QICFields.WAREHOUSE_LOCATION);
        Entity ngLocation = qic.getBelongsToField(QICFields.NG_LOCATION);

        switch (decision) {
            case QICFields.QualityDecision.ACCEPT:
                createSingleDocument(documentDD, qic, locationFrom, warehouseLocation, QICFields.WAREHOUSE_QUANTITY);
                break;
            case QICFields.QualityDecision.REJECT:
                createSingleDocument(documentDD, qic, locationFrom, ngLocation, QICFields.NG_QUANTITY);
                break;
            case QICFields.QualityDecision.PARTIAL:
                createSingleDocument(documentDD, qic, locationFrom, warehouseLocation, QICFields.WAREHOUSE_QUANTITY);
                createSingleDocument(documentDD, qic, locationFrom, ngLocation, QICFields.NG_QUANTITY);
                break;
            default:
                break;
        }
    }

    private void createSingleDocument(
            DataDefinition documentDD,
            Entity qic,
            Entity locationFrom,
            Entity locationTo,
            String quantityFieldName
    ) {
        if (locationTo == null) {
            return;
        }

        try {
            Entity newDoc = documentDD.create();
            newDoc.setField("name", "Transfer from QIC " + qic.getStringField(QICFields.PO_NUMBER));
            newDoc.setField("type", "05transfer");
            newDoc.setField("time", java.util.Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()));
            newDoc.setField("locationFrom", locationFrom);
            newDoc.setField("locationTo", locationTo);
            newDoc.setField("company", qic.getBelongsToField(QICFields.COMPANY));
            newDoc.setField("user", securityService.getCurrentUserId());
            newDoc.setField("description", "Auto created from QIC transfer - PO " + qic.getStringField(QICFields.PO_NUMBER));
            newDoc.setField("state", "02accepted");

            newDoc = documentDD.save(newDoc); // Gán lại newDoc

            createPosition(newDoc, qic, quantityFieldName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createPosition(Entity document, Entity qic, String quantityFieldName) {
        DataDefinition positionDD = dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER, MaterialFlowResourcesConstants.MODEL_POSITION);
        Entity pos = positionDD.create();
        pos.setField("document", document);
        pos.setField("product", qic.getBelongsToField(QICFields.PRODUCT));
        pos.setField("quantity", qic.getField(quantityFieldName));
        pos.setField("resourceNumber", generateResourceNumber());
        pos = positionDD.save(pos);

        createResourceFromPosition(pos, document);
    }

    private void createResourceFromPosition(Entity pos, Entity document) {
        DataDefinition resourceDD = dataDefinitionService.get(
                MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                MaterialFlowResourcesConstants.MODEL_RESOURCE
        );

        Entity res = resourceDD.create();
        res.setField("number", pos.getStringField("resourceNumber"));
        res.setField("location", document.getBelongsToField("locationTo"));
        res.setField("product", pos.getBelongsToField("product"));
        res.setField("quantity", pos.getDecimalField("quantity"));
        res.setField("time", document.getDateField("time"));
        resourceDD.save(res);
    }

    private String generateResourceNumber() {
        DataDefinition positionDD = dataDefinitionService.get(
                MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                MaterialFlowResourcesConstants.MODEL_POSITION
        );

        // 1. Lấy bản ghi mới nhất
        Entity lastPos = positionDD.find()
                .addOrder(SearchOrders.desc("resourceNumber"))
                .setMaxResults(1)
                .uniqueResult();

        int currentYear = LocalDate.now().getYear();  // ví dụ 2025
        int nextNumber = 1; // default khi không có dữ liệu cũ

        if (lastPos != null) {
            String lastCode = lastPos.getStringField("resourceNumber");   // vd: 2025/00979

            if (lastCode != null && lastCode.contains("/")) {
                String[] parts = lastCode.split("/");

                int year = Integer.parseInt(parts[0]);        // 2025
                int number = Integer.parseInt(parts[1]);      // 979

                if (year == currentYear) {
                    nextNumber = number + 1;                  // +1 nếu cùng năm
                }
            }
        }

        return String.format("%d/%05d", currentYear, nextNumber);
    }

    private void updateQICStatusToCompleted(Entity qic) {
        try {
            qic.setField(QICFields.STATUS, QICFields.Status.COMPLETED);
            DataDefinition qicDD = dataDefinitionService.get(QMConstants.PLUGIN_IDENTIFIER, QMConstants.MODEL_QUALITY_INSPECTION_COMMAND);
            qicDD.save(qic);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

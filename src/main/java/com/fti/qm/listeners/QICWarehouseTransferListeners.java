package com.fti.qm.listeners;

import com.fti.qm.constants.QMConstants;
import com.fti.qm.constants.qualityInspectionCommand.QICFields;
import com.qcadoo.mes.materialFlowResources.constants.MaterialFlowResourcesConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.security.api.SecurityService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Listener xử lý nghiệp vụ chuyển kho (warehouse transfer) cho QIC.
 * Chỉ thực hiện khi QIC ở trạng thái IN_PROGRESS.
 *
 * Chức năng gồm:
 * - Tạo tài liệu chuyển kho theo Quality Decision.
 * - Tạo position và resource tương ứng.
 * - Cập nhật trạng thái QIC sang COMPLETED.
 */
@Service
public class QICWarehouseTransferListeners {
    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private SecurityService securityService;

    /**
     * Action chuyển kho từ giao diện:
     * - Kiểm tra QIC hợp lệ và đang IN_PROGRESS.
     * - Tạo document, position, resource theo quyết định chất lượng.
     * - Cập nhật trạng thái QIC -> COMPLETED.
     */
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

    /**
     * Tạo các document dựa trên qualityDecision:
     * - ACCEPT  → chuyển vào warehouseLocation.
     * - REJECT  → chuyển vào ngLocation.
     * - PARTIAL → tạo cả 2 document.
     */
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

    /**
     * Tạo Document chuyển kho tự động từ QIC và sinh Position + Resource tương ứng.
     *
     * @param documentDD DataDefinition của Document
     * @param qic Quality Inspection Command
     * @param locationFrom kho nguồn
     * @param locationTo kho đích
     * @param quantityFieldName field số lượng dùng để tạo Position/Resource
     */
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

            Entity newResource = createResource(newDoc, qic, quantityFieldName);
            createPosition(newDoc, qic, quantityFieldName, newResource);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Tạo Position cho Document chuyển kho và liên kết với Resource đã tạo.
     *
     * @param document Document chuyển kho
     * @param qic Quality Inspection Command
     * @param quantityFieldName field số lượng trong QIC
     * @param resource Resource đã được tạo để liên kết
     */
    private void createPosition(Entity document, Entity qic, String quantityFieldName, Entity resource) {
        DataDefinition positionDD = dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER, MaterialFlowResourcesConstants.MODEL_POSITION);
        Entity pos = positionDD.create();
        pos.setField("document", document);
        pos.setField("product", qic.getBelongsToField(QICFields.PRODUCT));
        pos.setField("quantity", qic.getField(quantityFieldName));
        pos.setField("givenUnit", qic.getBelongsToField("product").getStringField("unit"));
        pos.setField("givenQuantity", qic.getField(quantityFieldName));
        pos.setField("conversion", 1);
        pos.setField("waste", false);
        pos.setField("resourceReceiptDocument", resource.getId());
        pos.setField("resourceNumber", resource.getStringField("number"));
        pos = positionDD.save(pos);
    }

    /**
     * Tạo Resource tại kho đích dựa trên Document và QIC.
     *
     * - Gán locationTo, product, quantity theo QIC
     * - Thiết lập đơn vị, hệ số chuyển đổi và thời gian
     * - Lưu thông tin người thực hiện và loại chứng từ
     *
     * @param document Document chuyển kho
     * @param qic Quality Inspection Command
     * @param quantityFieldName field số lượng trong QIC
     * @return Resource đã được lưu
     * @throws IllegalStateException nếu lưu Resource thất bại
     */
    private Entity createResource(Entity document, Entity qic, String quantityFieldName) {
        DataDefinition resourceDD = dataDefinitionService.get(
                MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                MaterialFlowResourcesConstants.MODEL_RESOURCE
        );

        Entity res = resourceDD.create();
        res.setField("location", document.getBelongsToField("locationTo"));
        res.setField("product", qic.getBelongsToField("product"));
        res.setField("quantity", qic.getField(quantityFieldName));
        res.setField("time", document.getDateField("time"));
        res.setField("quantityInAdditionalUnit", qic.getField(quantityFieldName));
        res.setField("conversion", 1);
        res.setField("givenUnit", qic.getBelongsToField("product").getStringField("unit"));
        res.setField("userName", qic.getBelongsToField("user").getStringField("userName"));
        res.setField("documentNumber", document.getStringField("number").split("/")[0]);

        Entity saved = resourceDD.save(res);

        if (!saved.isValid()) {
            throw new IllegalStateException(
                    "Không thể tạo Resource: " + saved.getGlobalErrors()
            );
        }

        return saved;
    }

    /**
     * Cập nhật trạng thái QIC sang COMPLETED.
     */
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

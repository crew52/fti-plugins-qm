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
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Listener xử lý chuyển kho cho Quality Inspection Command (QIC).
 *
 * <p>
 * Được gọi từ giao diện QIC khi thực hiện action Transfer Warehouse.
 * Chỉ áp dụng cho QIC ở trạng thái {@code IN_PROGRESS}.
 * </p>
 *
 * <p>
 * Dựa trên {@code qualityDecision}, listener tạo Document chuyển kho (DRAFT)
 * và Position tương ứng. Việc cập nhật tồn kho và sinh Resource
 * được engine {@code materialFlowResources} xử lý khi Document được ACCEPT.
 * </p>
 *
 * <p>
 * Sau khi tạo Document, trạng thái QIC được cập nhật sang {@code COMPLETED}.
 * </p>
 */
@Service
public class QICWarehouseTransferListeners {
    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private SecurityService securityService;

    /**
     * Thực hiện chuyển kho cho QIC từ giao diện.
     *
     * <p>
     * Kiểm tra trạng thái QIC, tạo Document chuyển kho theo Quality Decision
     * và cập nhật QIC sang {@code COMPLETED}.
     * </p>
     */
    @Transactional
    public void transferWarehouse(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        try {
            FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
            Long id = form.getEntityId();

            if (id == null) {
                view.addMessage("qm.qic.transferWarehouse.noData", ComponentState.MessageType.FAILURE);
                return;
            }

            if (!validateTransferData(view)) {
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

        } catch (Exception e) {
            view.addMessage("qm.qic.transferWarehouse.error",
                    ComponentState.MessageType.FAILURE);
            throw e;
        }
    }

    /**
     * Tạo Document chuyển kho dựa trên Quality Decision của QIC:
     * ACCEPT, REJECT hoặc PARTIAL.
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
     * Tạo một Document chuyển kho (DRAFT) và Position tương ứng từ QIC.
     *
     * @param documentDD DataDefinition của Document
     * @param qic        Quality Inspection Command
     * @param locationFrom kho nguồn
     * @param locationTo   kho đích
     * @param quantityFieldName field số lượng trong QIC
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
            newDoc.setField("state", "01draft");

            newDoc = documentDD.save(newDoc); // Gán lại newDoc

            createPosition(newDoc, qic, quantityFieldName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Tạo Position cho Document chuyển kho dựa trên thông tin QIC.
     */
    private void createPosition(Entity document, Entity qic, String quantityFieldName) {
        DataDefinition positionDD = dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER, MaterialFlowResourcesConstants.MODEL_POSITION);
        Entity pos = positionDD.create();
        pos.setField("document", document);
        pos.setField("product", qic.getBelongsToField(QICFields.PRODUCT));
        pos.setField("quantity", qic.getField(quantityFieldName));
        pos.setField("givenUnit", qic.getBelongsToField("product").getStringField("unit"));
        pos = positionDD.save(pos);
    }

    /**
     * Cập nhật trạng thái QIC sang {@code COMPLETED}.
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

    /**
     * Validate dữ liệu chuyển kho trên giao diện trước khi tạo Document.
     *
     * <p>
     * Kiểm tra các trường bắt buộc theo Quality Decision:
     * ACCEPT, REJECT hoặc PARTIAL.
     * Nếu thiếu dữ liệu, hiển thị thông báo lỗi và dừng xử lý.
     * </p>
     *
     * @param view trạng thái hiện tại của màn hình
     * @return {@code true} nếu dữ liệu hợp lệ, ngược lại {@code false}
     */
    private boolean validateTransferData(final ViewDefinitionState view) {

        FieldComponent decisionField = (FieldComponent) view.getComponentByReference(QICFields.QUALITY_DECISION);
        FieldComponent warehouseQty = (FieldComponent) view.getComponentByReference(QICFields.WAREHOUSE_QUANTITY);
        LookupComponent warehouseLoc = (LookupComponent) view.getComponentByReference(QICFields.WAREHOUSE_LOCATION);
        FieldComponent ngQty = (FieldComponent) view.getComponentByReference(QICFields.NG_QUANTITY);
        LookupComponent ngLoc = (LookupComponent) view.getComponentByReference(QICFields.NG_LOCATION);

        String decision = decisionField.getFieldValue() != null
                ? decisionField.getFieldValue().toString().trim()
                : "";

        if (decision.isEmpty()) {
            view.addMessage("qm.qic.transferWarehouse.requiredData", ComponentState.MessageType.FAILURE);
            return false;
        }

        Object warehouseQtyValue = warehouseQty.getFieldValue();
        Object ngQtyValue = ngQty.getFieldValue();

        switch (decision) {

            case QICFields.QualityDecision.ACCEPT:
                if (warehouseLoc.getEntity() == null || warehouseQtyValue == null) {
                    view.addMessage("qm.qic.transferWarehouse.requiredData", ComponentState.MessageType.FAILURE);
                    return false;
                }
                break;

            case QICFields.QualityDecision.REJECT:
                if (ngLoc.getEntity() == null || ngQtyValue == null) {
                    view.addMessage("qm.qic.transferWarehouse.requiredData", ComponentState.MessageType.FAILURE);
                    return false;
                }
                break;

            case QICFields.QualityDecision.PARTIAL:
                if (warehouseLoc.getEntity() == null || warehouseQtyValue == null || ngLoc.getEntity() == null || ngQtyValue == null) {
                    view.addMessage("qm.qic.transferWarehouse.requiredData", ComponentState.MessageType.FAILURE);
                    return false;
                }
                break;

            default:
                view.addMessage("qm.qic.transferWarehouse.requiredData", ComponentState.MessageType.FAILURE);
                return false;
        }

        return true;
    }
}

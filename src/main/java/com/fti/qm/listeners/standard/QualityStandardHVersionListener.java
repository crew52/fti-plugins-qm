package com.fti.qm.listeners.standard;

import com.fti.qm.constants.QMConstants;
import com.fti.qm.constants.qualityInspectionCommand.QICFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchOrders;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class QualityStandardHVersionListener {
    @Autowired
    private DataDefinitionService dataDefinitionService;

    /**
     * Listener được gọi từ nút "Tạo phiên bản mới".
     * Ở bước đầu tiên: kiểm tra xem QIC cho product + 01incoming có tồn tại hay không.
     */
    public void createVersion(final ViewDefinitionState view, final ComponentState button, final String[] args) {
        FormComponent form = (FormComponent) view.getComponentByReference("form");
        Entity qsh = form.getEntity();

        if (qsh == null || qsh.getId() == null) {
            view.addMessage("qm.qualityStandardH.error.noRecord", ComponentState.MessageType.FAILURE);
            return;
        }

        // Lấy product từ QualityStandardH
        Entity product = qsh.getBelongsToField("product");
        if (product == null || product.getId() == null) {
            view.addMessage("qm.qualityStandardH.error.noProduct", ComponentState.MessageType.FAILURE);
            return;
        }

        DataDefinition qicDD = dataDefinitionService.get(QMConstants.PLUGIN_IDENTIFIER, QMConstants.MODEL_QUALITY_INSPECTION_COMMAND);
        if (qicDD == null) {
            view.addMessage("qm.qualityStandardH.error.cannotAccessQIC", ComponentState.MessageType.FAILURE);
            return;
        }

        // Lấy QIC cũ (product + 01incoming)
        Entity oldQIC = qicDD.find()
                .add(SearchRestrictions.eq("product.id", product.getId()))
                .add(SearchRestrictions.eq(QICFields.INSPECTION_TYPE, QICFields.INSPECTION_TYPE_INCOMING))
                .addOrder(SearchOrders.desc("version"))
                .setMaxResults(1)
                .uniqueResult();

        if (oldQIC == null) {
            // TH1: Không tìm thấy QIC
            view.addMessage("qm.qualityStandardH.info.qicNotFound", ComponentState.MessageType.INFO);
            return;
        }

        // TH2: Tìm thấy QIC -> tạo QIC mới dựa trên QIC cũ
        Entity newQIC = qicDD.create();

        // Copy dữ liệu từ QIC cũ
        newQIC.setField("inspectionType", oldQIC.getField("inspectionType"));
        newQIC.setField("status", "01new"); // trạng thái mới
        newQIC.setField("productionOrderNumber", oldQIC.getField("productionOrderNumber"));
        newQIC.setField("operationNumber", oldQIC.getField("operationNumber"));
        newQIC.setField("company", oldQIC.getField("company"));
        newQIC.setField("product", oldQIC.getField("product"));
        newQIC.setField("tool", oldQIC.getField("tool"));
        newQIC.setField("inspectionOrderNumber", oldQIC.getField("inspectionOrderNumber"));
        newQIC.setField("inspectionOrderNumberInt", oldQIC.getField("inspectionOrderNumberInt"));
        newQIC.setField("poNumber", oldQIC.getField("poNumber"));
        newQIC.setField("executionDate", oldQIC.getField("executionDate"));
        newQIC.setField("transactionQuantity", oldQIC.getField("transactionQuantity"));
        newQIC.setField("location", oldQIC.getField("location"));
        newQIC.setField("qualityDecision", oldQIC.getField("qualityDecision"));
        newQIC.setField("warehouseQuantity", oldQIC.getField("warehouseQuantity"));
        newQIC.setField("ngQuantity", oldQIC.getField("ngQuantity"));
        newQIC.setField("warehouseLocation", oldQIC.getField("warehouseLocation"));
        newQIC.setField("ngLocation", oldQIC.getField("ngLocation"));

        // Version mới = version cũ + 1
        Integer oldVersion = oldQIC.getIntegerField("version");
        newQIC.setField("version", oldVersion != null ? oldVersion + 1 : 1);

        // Ngày tạo mới
        newQIC.setField("createdDate", new Date());

        // Lưu QIC mới
        qicDD.save(newQIC);

        view.addMessage("qm.qualityStandardH.info.newQicCreated", ComponentState.MessageType.SUCCESS);
    }

    /**
     * Kiểm tra có QIC nào cho productId với inspectionType = 01incoming hay không.
     */
    private boolean qicExistsForProductIncoming(final Long productId) {
        if (productId == null) return false;

        DataDefinition qicDD = dataDefinitionService.get(QMConstants.PLUGIN_IDENTIFIER, QMConstants.MODEL_QUALITY_INSPECTION_COMMAND);
        if (qicDD == null) return false;

        List<?> qics = qicDD.find()
                .add(SearchRestrictions.eq("product.id", productId))
                .add(SearchRestrictions.eq(QICFields.INSPECTION_TYPE, QICFields.INSPECTION_TYPE_INCOMING))
                .setMaxResults(1)
                .list()
                .getEntities();

        return qics != null && !qics.isEmpty();
    }
}

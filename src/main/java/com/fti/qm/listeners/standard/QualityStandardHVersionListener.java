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

import java.util.*;
import java.util.stream.Collectors;

@Service
public class QualityStandardHVersionListener {
    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void createVersion(final ViewDefinitionState view, final ComponentState button, final String[] args) {
        FormComponent form = (FormComponent) view.getComponentByReference("form");
        Entity qshForm = form.getEntity();

        if (qshForm == null || qshForm.getId() == null) {
            view.addMessage("qm.qualityStandardH.error.noRecord", ComponentState.MessageType.FAILURE);
            return;
        }

        // Reload QSH từ DB để có hasMany
        Entity qsh = qshForm.getDataDefinition().get(qshForm.getId());

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
            view.addMessage("qm.qualityStandardH.info.qicNotFound", ComponentState.MessageType.INFO);
            return;
        }

        // Lấy danh sách qualityStandardSample từ QIC cũ
        List<Entity> oldSampleLs = oldQIC.getHasManyField("qualityStandardSamplesRe");

        // Lấy danh sách qualityStandardL hiện tại từ QSH (header)
        List<Entity> currentLs = qsh.getHasManyField("qualityStandardLs");

        // Kiểm tra xem có L mới nào chưa có sample
        Set<Long> oldLIds = oldSampleLs.stream()
                .map(s -> s.getBelongsToField("qualityStandardL"))
                .filter(Objects::nonNull)
                .map(Entity::getId)
                .collect(Collectors.toSet());

        boolean hasNewL = currentLs.stream().anyMatch(l -> !oldLIds.contains(l.getId()));
        if (!hasNewL) {
            view.addMessage("qm.qualityStandardH.info.noNewL", ComponentState.MessageType.INFO);
            return; // Không tạo QIC mới nếu không có Line mới
        }

        // Tạo QIC mới
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
        newQIC = qicDD.save(newQIC);

        // ==============================================
        // ====== THÊM SAMPLE SAU KHI TẠO QIC MỚI =======
        // ==============================================

        DataDefinition sampleDD = dataDefinitionService.get(QMConstants.PLUGIN_IDENTIFIER, QMConstants.MODEL_QUALITY_STANDARD_SAMPLE);

        // ========= 1. COPY TOÀN BỘ SAMPLE CŨ SANG QIC MỚI =========
        for (Entity oldSample : oldSampleLs) {
            Entity newSample = sampleDD.create();
            newSample.setField("qualityInspectionCommandRe", newQIC);
            newSample.setField("qualityStandardL", oldSample.getBelongsToField("qualityStandardL"));
            newSample.setField("sampleNumber", oldSample.getIntegerField("sampleNumber"));
            newSample.setField("qualitativeResult", oldSample.getStringField("qualitativeResult"));
            newSample.setField("quantitativeResult", oldSample.getDecimalField("quantitativeResult"));
            newSample.setField("quantitativeEvaluation", oldSample.getStringField("quantitativeEvaluation"));
            sampleDD.save(newSample);
        }

        // ========= 2. TẠO SAMPLE CHO L MỚI =========
        for (Entity currentL : currentLs) {
            Long currentLId = currentL.getId();
            if (!oldLIds.contains(currentLId)) {
                Integer sampleSize = currentL.getIntegerField("sampleSize");
                if (sampleSize == null || sampleSize <= 0) sampleSize = 1;

                for (int i = 1; i <= sampleSize; i++) {
                    Entity newSample = sampleDD.create();
                    newSample.setField("qualityInspectionCommandRe", newQIC);
                    newSample.setField("qualityStandardL", currentL);
                    newSample.setField("sampleNumber", i);
                    sampleDD.save(newSample);
                }
            }
        }

        view.addMessage("qm.qualityStandardH.info.newQicCreated", ComponentState.MessageType.SUCCESS);
    }
}

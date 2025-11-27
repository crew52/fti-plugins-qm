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

    /**
     * Listener được gọi từ nút "Tạo phiên bản mới".
     * Ở bước đầu tiên: kiểm tra xem QIC cho product + 01incoming có tồn tại hay không.
     */
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
            // TH1: Không tìm thấy QIC
            view.addMessage("qm.qualityStandardH.info.qicNotFound", ComponentState.MessageType.INFO);
            return;
        }

        // Lấy danh sách qualityStandardSample từ QIC cũ
        List<Entity> oldSampleLs = oldQIC.getHasManyField("qualityStandardSamplesRe");

// Lấy danh sách qualityStandardL hiện tại từ QSH (header)
        List<Entity> currentLs = qsh.getHasManyField("qualityStandardLs");

// DEBUG: In ra danh sách currentLs
        System.out.println("===== CURRENT Ls FROM QSH =====");
        for (Entity c : currentLs) {
            System.out.println("Current L ID = " + c.getId()
                    + ", Criteria = " + (c.getBelongsToField("qualityCriteria") != null
                    ? c.getBelongsToField("qualityCriteria").getId()
                    : "null")
                    + ", SampleSize = " + c.getIntegerField("sampleSize"));
        }

// DEBUG: In ra danh sách L trong oldSampleLs
        System.out.println("===== OLD SAMPLE Ls FROM OLD QIC =====");
        for (Entity s : oldSampleLs) {
            Entity l = s.getBelongsToField("qualityStandardL");
            System.out.println("Sample ID = " + s.getId()
                    + ", L ID = " + (l != null ? l.getId() : "null")
                    + ", SampleNumber = " + s.getIntegerField("sampleNumber"));
        }

// Kiểm tra xem có L mới trong currentLs mà oldSampleLs không có hay không
        boolean hasNewL = currentLs.stream().anyMatch(currentL -> {
            Long currentLId = currentL.getId();

            System.out.println("Check L ID in currentLs = " + currentLId);

            boolean exists = oldSampleLs.stream()
                    .map(s -> s.getBelongsToField("qualityStandardL"))
                    .filter(l -> l != null)
                    .map(Entity::getId)
                    .anyMatch(id -> {
                        System.out.println("    Compare with old L ID = " + id);
                        return id.equals(currentLId);
                    });

            System.out.println(" => Exists in OLD? " + exists);
            return !exists;
        });

        System.out.println("===== Final Result: hasNewL = " + hasNewL + " =====");

        if (!hasNewL) {
            view.addMessage("qm.qualityStandardH.info.noNewL", ComponentState.MessageType.INFO);
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
        newQIC = qicDD.save(newQIC);

        // ==============================================
// ====== THÊM SAMPLE SAU KHI TẠO QIC MỚI =======
// ==============================================

        DataDefinition sampleDD =
                dataDefinitionService.get(QMConstants.PLUGIN_IDENTIFIER, QMConstants.MODEL_QUALITY_STANDARD_SAMPLE);

// Gom sample cũ theo L
        Map<Long, List<Entity>> oldSamplesByL = oldSampleLs.stream()
                .filter(s -> s.getBelongsToField("qualityStandardL") != null)
                .collect(Collectors.groupingBy(s -> s.getBelongsToField("qualityStandardL").getId()));

        System.out.println("===== COPYING OLD SAMPLES INTO NEW QIC =====");

// ========= 1. COPY SAMPLE CŨ SANG QIC MỚI =========
        for (Map.Entry<Long, List<Entity>> entry : oldSamplesByL.entrySet()) {
            for (Entity oldSample : entry.getValue()) {

                Entity newSample = sampleDD.create();

                newSample.setField("qualityInspectionCommandRe", newQIC);
                newSample.setField("qualityStandardL", oldSample.getBelongsToField("qualityStandardL"));
                newSample.setField("sampleNumber", oldSample.getIntegerField("sampleNumber"));

                sampleDD.save(newSample);

                System.out.println("Copied sample: L=" +
                        oldSample.getBelongsToField("qualityStandardL").getId() +
                        ", sampleNumber=" + oldSample.getIntegerField("sampleNumber"));
            }
        }

// ========= 2. TẠO SAMPLE CHO L MỚI =========

        Set<Long> oldLIds = oldSamplesByL.keySet();

        System.out.println("===== CREATING SAMPLE FOR NEW Ls =====");

        for (Entity currentL : currentLs) {

            Long currentLId = currentL.getId();

            // L mới (không tồn tại trong sample cũ)
            if (!oldLIds.contains(currentLId)) {

                Integer sampleSize = currentL.getIntegerField("sampleSize");
                if (sampleSize == null || sampleSize <= 0) {
                    sampleSize = 1; // default
                }

                System.out.println("Create new samples for L = " + currentLId + ", sampleSize = " + sampleSize);

                for (int i = 1; i <= sampleSize; i++) {
                    Entity newSample = sampleDD.create();

                    newSample.setField("qualityInspectionCommandRe", newQIC);
                    newSample.setField("qualityStandardL", currentL);
                    newSample.setField("sampleNumber", i);

                    sampleDD.save(newSample);

                    System.out.println("Created new sample: L=" + currentLId + ", sampleNumber=" + i);
                }
            }
        }

        System.out.println("===== DONE CREATING SAMPLES FOR NEW QIC =====");

        view.addMessage("qm.qualityStandardH.info.newQicCreated", ComponentState.MessageType.SUCCESS);
    }
}

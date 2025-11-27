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
        try {
            Entity qsh = getQualityStandardH(view);
            Entity product = getProduct(view, qsh);
            Entity oldQIC = getLatestQIC(view, product);

            // Lấy Line và Sample
            List<Entity> oldSamples = oldQIC.getHasManyField("qualityStandardSamplesRe");
            List<Entity> currentLs = qsh.getHasManyField("qualityStandardLs");

            // Kiểm tra có L mới
            if (!hasNewLine(oldSamples, currentLs)) {
                view.addMessage("qm.qualityStandardH.info.noNewL", ComponentState.MessageType.INFO);
                return;
            }

            // Kiểm tra version 1 và chưa có sample
            Entity targetQIC;
            if (isVersion1WithoutSamples(oldQIC)) {
                // Thêm sample vào version 1
                targetQIC = oldQIC;
            } else {
                // Tạo QIC mới
                targetQIC = createNewQIC(oldQIC);
            }

            // Copy sample cũ sang QIC mới (nếu tạo version mới)
            if (targetQIC != oldQIC) {
                copyOldSamplesToNewQIC(oldSamples, targetQIC);
            }

            // Tạo sample cho L mới
            createSamplesForNewLs(currentLs, oldSamples, targetQIC);

            view.addMessage("qm.qualityStandardH.info.newQicCreated", ComponentState.MessageType.SUCCESS);
        } catch (Exception ex) {
            view.addMessage("qm.qualityStandardH.error.creationFailed", ComponentState.MessageType.FAILURE);
            ex.printStackTrace();
        }
    }

    private boolean isVersion1WithoutSamples(Entity qic) {
        Integer version = qic.getIntegerField("version");
        List<Entity> samples = qic.getHasManyField("qualityStandardSamplesRe");
        return version != null && version == 1 && (samples == null || samples.isEmpty());
    }

    private Entity getQualityStandardH(ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference("form");
        Entity qshForm = form.getEntity();
        if (qshForm == null || qshForm.getId() == null) {
            view.addMessage("qm.qualityStandardH.error.noRecord", ComponentState.MessageType.FAILURE);
            return null;
        }
        return qshForm.getDataDefinition().get(qshForm.getId());
    }

    private Entity getProduct(ViewDefinitionState view, Entity qsh) {
        Entity product = qsh.getBelongsToField("product");
        if (product == null || product.getId() == null) {
            view.addMessage("qm.qualityStandardH.error.noProduct", ComponentState.MessageType.FAILURE);
            return null;
        }
        return product;
    }

    private Entity getLatestQIC(ViewDefinitionState view, Entity product) {
        DataDefinition qicDD = dataDefinitionService.get(QMConstants.PLUGIN_IDENTIFIER, QMConstants.MODEL_QUALITY_INSPECTION_COMMAND);
        if (qicDD == null) {
            view.addMessage("qm.qualityStandardH.error.cannotAccessQIC", ComponentState.MessageType.FAILURE);
            return null;
        }
        Entity oldQIC = qicDD.find()
                .add(SearchRestrictions.eq("product.id", product.getId()))
                .add(SearchRestrictions.eq(QICFields.INSPECTION_TYPE, QICFields.INSPECTION_TYPE_INCOMING))
                .addOrder(SearchOrders.desc("version"))
                .setMaxResults(1)
                .uniqueResult();
        if (oldQIC == null) {
            view.addMessage("qm.qualityStandardH.info.qicNotFound", ComponentState.MessageType.INFO);
        }
        return oldQIC;
    }

    private boolean hasNewLine(List<Entity> oldSamples, List<Entity> currentLs) {
        Set<Long> oldLIds = oldSamples.stream()
                .map(s -> s.getBelongsToField("qualityStandardL"))
                .filter(Objects::nonNull)
                .map(Entity::getId)
                .collect(Collectors.toSet());

        return currentLs.stream().anyMatch(l -> !oldLIds.contains(l.getId()));
    }

    private Entity createNewQIC(Entity oldQIC) {
        DataDefinition qicDD = oldQIC.getDataDefinition();
        Entity newQIC = qicDD.create();

        newQIC.setField("inspectionType", oldQIC.getField("inspectionType"));
        newQIC.setField("status", "01new");
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

        Integer oldVersion = oldQIC.getIntegerField("version");
        newQIC.setField("version", oldVersion != null ? oldVersion + 1 : 1);
        newQIC.setField("createdDate", new Date());

        return qicDD.save(newQIC);
    }

    private void copyOldSamplesToNewQIC(List<Entity> oldSamples, Entity newQIC) {
        DataDefinition sampleDD = dataDefinitionService.get(QMConstants.PLUGIN_IDENTIFIER, QMConstants.MODEL_QUALITY_STANDARD_SAMPLE);
        for (Entity oldSample : oldSamples) {
            Entity newSample = sampleDD.create();
            newSample.setField("qualityInspectionCommandRe", newQIC);
            newSample.setField("qualityStandardL", oldSample.getBelongsToField("qualityStandardL"));
            newSample.setField("sampleNumber", oldSample.getIntegerField("sampleNumber"));
            newSample.setField("qualitativeResult", oldSample.getStringField("qualitativeResult"));
            newSample.setField("quantitativeResult", oldSample.getDecimalField("quantitativeResult"));
            newSample.setField("quantitativeEvaluation", oldSample.getStringField("quantitativeEvaluation"));
            sampleDD.save(newSample);
        }
    }

    private void createSamplesForNewLs(List<Entity> currentLs, List<Entity> oldSamples, Entity newQIC) {
        Set<Long> oldLIds = oldSamples.stream()
                .map(s -> s.getBelongsToField("qualityStandardL"))
                .filter(Objects::nonNull)
                .map(Entity::getId)
                .collect(Collectors.toSet());

        DataDefinition sampleDD = dataDefinitionService.get(QMConstants.PLUGIN_IDENTIFIER, QMConstants.MODEL_QUALITY_STANDARD_SAMPLE);

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
    }
}

package com.fti.qm.listeners.standard;

import com.fti.qm.constants.QMConstants;
import com.fti.qm.constants.QSHFields;
import com.fti.qm.constants.QSLFields;
import com.fti.qm.constants.QualityStandardSampleFields;
import com.fti.qm.constants.qualityInspectionCommand.QICFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchOrders;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Listener để xử lý việc tạo phiên bản mới (Version) của Quality Standard H (QSH)
 * và các liên quan đến Quality Inspection Command (QIC) và Sample.
 * <p>
 * Quy tắc nghiệp vụ:
 * - Nếu có Line mới chưa tồn tại Sample, sẽ tạo QIC mới hoặc thêm sample.
 * - Nếu QIC cũ là version 1 và chưa có sample nào, sẽ không tạo version mới mà thêm sample trực tiếp.
 */
@Service
public class QualityStandardHVersionListener {
    @Autowired
    private DataDefinitionService dataDefinitionService;

    /**
     * Tạo phiên bản mới của QIC dựa trên Quality Standard H hiện tại.
     * <p>
     * Quy trình:
     * 1. Lấy QSH và Product.
     * 2. Lấy QIC cũ mới nhất.
     * 3. Kiểm tra Line mới.
     * 4. Tạo QIC mới nếu cần hoặc thêm sample vào QIC version 1 chưa có sample.
     * 5. Copy sample cũ và tạo sample cho Line mới.
     *
     * @param view   View hiện tại
     * @param button ComponentState của button (không sử dụng trực tiếp)
     * @param args   Tham số bổ sung (không sử dụng trực tiếp)
     */
    public void createVersion(final ViewDefinitionState view, final ComponentState button, final String[] args) {
        try {
            Entity qsh = getQualityStandardH(view);
            Entity product = getProduct(view, qsh);
            Entity oldQIC = getLatestQIC(view, product);

            // Lấy Line và Sample
            List<Entity> oldSamples = oldQIC.getHasManyField(QICFields.QUALITY_STANDARD_SAMPLES_RES);

            List<Entity> currentLs = qsh.getHasManyField(QSHFields.QUALITY_STANDARD_LS)
                    .stream()
                    .filter(l -> !Boolean.TRUE.equals(l.getBooleanField("deleted")))
                    .collect(Collectors.toList());

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

    /**
     * Kiểm tra QIC version 1 và chưa có sample nào.
     *
     * @param qic QIC cần kiểm tra
     * @return true nếu QIC là version 1 và chưa có sample nào, false nếu không
     */
    private boolean isVersion1WithoutSamples(Entity qic) {
        Integer version = qic.getIntegerField(QICFields.VERSION);
        List<Entity> samples = qic.getHasManyField(QICFields.QUALITY_STANDARD_SAMPLES_RES);
        return version != null && version == 1 && (samples == null || samples.isEmpty());
    }

    /**
     * Lấy Quality Standard H hiện tại từ Form.
     *
     * @param view View hiện tại
     * @return Entity của Quality Standard H hoặc null nếu không tìm thấy
     */
    private Entity getQualityStandardH(ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        Entity qshForm = form.getEntity();
        if (qshForm == null || qshForm.getId() == null) {
            view.addMessage("qm.qualityStandardH.error.noRecord", ComponentState.MessageType.FAILURE);
            return null;
        }
        return qshForm.getDataDefinition().get(qshForm.getId());
    }

    /**
     * Lấy Product liên quan đến Quality Standard H.
     *
     * @param view View hiện tại
     * @param qsh  Entity Quality Standard H
     * @return Entity Product hoặc null nếu không tìm thấy
     */
    private Entity getProduct(ViewDefinitionState view, Entity qsh) {
        Entity product = qsh.getBelongsToField(QSHFields.PRODUCT);
        if (product == null || product.getId() == null) {
            view.addMessage("qm.qualityStandardH.error.noProduct", ComponentState.MessageType.FAILURE);
            return null;
        }
        return product;
    }

    /**
     * Lấy QIC mới nhất theo Product và loại kiểm tra incoming.
     *
     * @param view    View hiện tại
     * @param product Product cần lấy QIC
     * @return QIC mới nhất hoặc null nếu không tìm thấy
     */
    private Entity getLatestQIC(ViewDefinitionState view, Entity product) {
        DataDefinition qicDD = dataDefinitionService.get(QMConstants.PLUGIN_IDENTIFIER, QMConstants.MODEL_QUALITY_INSPECTION_COMMAND);
        if (qicDD == null) {
            view.addMessage("qm.qualityStandardH.error.cannotAccessQIC", ComponentState.MessageType.FAILURE);
            return null;
        }
        Entity oldQIC = qicDD.find()
                .add(SearchRestrictions.eq(QICFields.PRODUCT + ".id", product.getId()))
                .add(SearchRestrictions.eq(QICFields.INSPECTION_TYPE, QICFields.InspectionType.INCOMING))
                .addOrder(SearchOrders.desc(QICFields.VERSION))
                .setMaxResults(1)
                .uniqueResult();
        if (oldQIC == null) {
            view.addMessage("qm.qualityStandardH.info.qicNotFound", ComponentState.MessageType.INFO);
        }
        return oldQIC;
    }

    /**
     * Kiểm tra xem có Line mới nào chưa có sample hay không.
     *
     * @param oldSamples List sample cũ
     * @param currentLs  List Line hiện tại
     * @return true nếu có Line mới, false nếu không
     */
    private boolean hasNewLine(List<Entity> oldSamples, List<Entity> currentLs) {
        Set<Long> oldLIds = oldSamples.stream()
                .map(s -> s.getBelongsToField(QualityStandardSampleFields.QUALITY_STANDARD_L))
                .filter(Objects::nonNull)
                .map(Entity::getId)
                .collect(Collectors.toSet());

        return currentLs.stream()
                .filter(l -> !Boolean.TRUE.equals(l.getBooleanField("deleted")))
                .anyMatch(l -> !oldLIds.contains(l.getId()));
    }

    /**
     * Tạo QIC mới dựa trên QIC cũ.
     *
     * @param oldQIC QIC cũ để sao chép dữ liệu
     * @return Entity QIC mới đã lưu
     */
    private Entity createNewQIC(Entity oldQIC) {
        DataDefinition qicDD = oldQIC.getDataDefinition();
        Entity newQIC = qicDD.create();

        newQIC.setField(QICFields.INSPECTION_TYPE, oldQIC.getField(QICFields.INSPECTION_TYPE));
        newQIC.setField(QICFields.STATUS, QICFields.Status.NEW);
        newQIC.setField(QICFields.PRODUCTION_ORDER_NUMBER, oldQIC.getField(QICFields.PRODUCTION_ORDER_NUMBER));
        newQIC.setField(QICFields.OPERATION_NUMBER, oldQIC.getField(QICFields.OPERATION_NUMBER));
        newQIC.setField(QICFields.COMPANY, oldQIC.getField(QICFields.COMPANY));
        newQIC.setField(QICFields.PRODUCT, oldQIC.getField(QICFields.PRODUCT));
        newQIC.setField(QICFields.TOOL, oldQIC.getField(QICFields.TOOL));
        newQIC.setField(QICFields.INSPECTION_ORDER_NUMBER, oldQIC.getField(QICFields.INSPECTION_ORDER_NUMBER));
        newQIC.setField(QICFields.INSPECTION_ORDER_NUMBER_INT, oldQIC.getField(QICFields.INSPECTION_ORDER_NUMBER_INT));
        newQIC.setField(QICFields.PO_NUMBER, oldQIC.getField(QICFields.PO_NUMBER));
        newQIC.setField(QICFields.EXECUTION_DATE, oldQIC.getField(QICFields.EXECUTION_DATE));
        newQIC.setField(QICFields.TRANSACTION_QUANTITY, oldQIC.getField(QICFields.TRANSACTION_QUANTITY));
        newQIC.setField(QICFields.LOCATION, oldQIC.getField(QICFields.LOCATION));
        newQIC.setField(QICFields.QUALITY_DECISION, oldQIC.getField(QICFields.QUALITY_DECISION));
        newQIC.setField(QICFields.WAREHOUSE_QUANTITY, oldQIC.getField(QICFields.WAREHOUSE_QUANTITY));
        newQIC.setField(QICFields.NG_QUANTITY, oldQIC.getField(QICFields.NG_QUANTITY));
        newQIC.setField(QICFields.WAREHOUSE_LOCATION, oldQIC.getField(QICFields.WAREHOUSE_LOCATION));
        newQIC.setField(QICFields.NG_QUANTITY, oldQIC.getField(QICFields.NG_QUANTITY));

        Integer oldVersion = oldQIC.getIntegerField(QICFields.VERSION);
        newQIC.setField(QICFields.VERSION, oldVersion != null ? oldVersion + 1 : 1);
        newQIC.setField(QICFields.CREATED_DATE, new Date());

        return qicDD.save(newQIC);
    }

    /**
     * Copy toàn bộ sample từ QIC cũ sang QIC mới.
     *
     * @param oldSamples List sample cũ
     * @param newQIC     QIC mới
     */
    private void copyOldSamplesToNewQIC(List<Entity> oldSamples, Entity newQIC) {
        DataDefinition sampleDD = dataDefinitionService.get(QMConstants.PLUGIN_IDENTIFIER, QMConstants.MODEL_QUALITY_STANDARD_SAMPLE);
        for (Entity oldSample : oldSamples) {
            Entity newSample = sampleDD.create();
            newSample.setField(QualityStandardSampleFields.QUALITY_INSPECTION_COMMAND, newQIC);
            newSample.setField(QualityStandardSampleFields.QUALITY_STANDARD_L, oldSample.getBelongsToField(QualityStandardSampleFields.QUALITY_STANDARD_L));
            newSample.setField(QualityStandardSampleFields.SAMPLE_NUMBER, oldSample.getIntegerField(QualityStandardSampleFields.SAMPLE_NUMBER));
            newSample.setField(QualityStandardSampleFields.QUALITATIVE_RESULT, oldSample.getStringField(QualityStandardSampleFields.QUALITATIVE_RESULT));
            newSample.setField(QualityStandardSampleFields.QUANTITATIVE_RESULT, oldSample.getDecimalField(QualityStandardSampleFields.QUANTITATIVE_RESULT));
            newSample.setField(QualityStandardSampleFields.QUANTITATIVE_EVALUATION, oldSample.getStringField(QualityStandardSampleFields.QUANTITATIVE_EVALUATION));
            sampleDD.save(newSample);
        }
    }

    /**
     * Tạo sample cho các Line mới chưa có sample.
     *
     * @param currentLs  List Line hiện tại
     * @param oldSamples List sample cũ
     * @param newQIC     QIC cần tạo sample
     */
    private void createSamplesForNewLs(List<Entity> currentLs, List<Entity> oldSamples, Entity newQIC) {
        Set<Long> oldLIds = oldSamples.stream()
                .map(s -> s.getBelongsToField(QualityStandardSampleFields.QUALITY_STANDARD_L))
                .filter(Objects::nonNull)
                .map(Entity::getId)
                .collect(Collectors.toSet());

        DataDefinition sampleDD = dataDefinitionService.get(QMConstants.PLUGIN_IDENTIFIER, QMConstants.MODEL_QUALITY_STANDARD_SAMPLE);

        for (Entity currentL : currentLs) {
            // Bỏ qua line bị deleted
            if (Boolean.TRUE.equals(currentL.getBooleanField("deleted"))) {
                continue;
            }
            Long currentLId = currentL.getId();
            if (!oldLIds.contains(currentLId)) {
                Integer sampleSize = currentL.getIntegerField(QSLFields.SAMPLE_SIZE);
                if (sampleSize == null || sampleSize <= 0) sampleSize = 1;

                for (int i = 1; i <= sampleSize; i++) {
                    Entity newSample = sampleDD.create();
                    newSample.setField(QualityStandardSampleFields.QUALITY_INSPECTION_COMMAND, newQIC);
                    newSample.setField(QualityStandardSampleFields.QUALITY_STANDARD_L, currentL);
                    newSample.setField(QualityStandardSampleFields.SAMPLE_NUMBER, i);
                    sampleDD.save(newSample);
                }
            }
        }
    }
}

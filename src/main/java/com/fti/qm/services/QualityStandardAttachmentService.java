package com.fti.qm.services;

import com.fti.qm.constants.*;
import com.fti.qm.constants.qualityInspectionCommand.QICFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class QualityStandardAttachmentService {
    @Autowired
    private DataDefinitionService dataDefinitionService;

    /**
     * Copy tất cả attachment từ StandardH sang QIC
     *
     * @param qic            QIC cần copy attachment
     * @param product        Product liên quan
     * @param inspectionType Loại kiểm tra
     * @param skipExistCheck Nếu true: bỏ qua check tồn tại (dùng khi QIC chưa có sample)
     */
    public void copyAttachments(Entity qic, Entity product, String inspectionType, boolean skipExistCheck) {

        DataDefinition hDD = dataDefinitionService.get(QMConstants.PLUGIN_IDENTIFIER, QMConstants.MODEL_QUALITY_STANDARD_H);

        List<Entity> hList = hDD.find()
                .add(SearchRestrictions.eq(GlobalFields.PRODUCT_ID, product.getId()))
                .add(SearchRestrictions.eq(QSHFields.TYPE, inspectionType))
                .add(SearchRestrictions.eq(GlobalFields.DELETED, false))
                .add(SearchRestrictions.eq(GlobalFields.ACTIVE, true))
                .list().getEntities();

        if (hList.isEmpty()) return;

        List<Long> hIds = hList.stream().map(Entity::getId).collect(Collectors.toList());

        DataDefinition stdAttachmentDD = dataDefinitionService.get(QMConstants.PLUGIN_IDENTIFIER, QMConstants.MODEL_QSH_ATTACHMENT);

        List<Entity> stdAttList = stdAttachmentDD.find()
                .add(SearchRestrictions.in(QSLFields.QUALITY_STANDARD_H_ID, hIds))
                .add(SearchRestrictions.eq(GlobalFields.DELETED, false))
                .list().getEntities();

        if (stdAttList.isEmpty()) return;

        DataDefinition qicAttachmentDD = dataDefinitionService.get(QMConstants.PLUGIN_IDENTIFIER, QMConstants.MODEL_QIC_ATTACHMENT);

        for (Entity stdAtt : stdAttList) {

            if (!skipExistCheck) {
                boolean exists = !qicAttachmentDD.find()
                        .add(SearchRestrictions.eq(QICFields.QUALITY_INSPECTION_COMMAND_ID, qic.getId()))
                        .add(SearchRestrictions.eq(QICAttachmentFields.ATTACHMENT, stdAtt.getStringField(QSHAttachmentFields.ATTACHMENT)))
                        .list().getEntities().isEmpty();
                if (exists) continue;
            }

            Entity newAtt = qicAttachmentDD.create();
            newAtt.setField(QICAttachmentFields.QUALITY_INSPECTION_COMMAND, qic);
            newAtt.setField(QICAttachmentFields.ATTACHMENT, stdAtt.getStringField(QSHAttachmentFields.ATTACHMENT));
            newAtt.setField(QICAttachmentFields.NAME, stdAtt.getStringField(QSHAttachmentFields.NAME));
            newAtt.setField(QICAttachmentFields.SIZE, stdAtt.getField(QSHAttachmentFields.SIZE));
            newAtt.setField(QICAttachmentFields.EXT, stdAtt.getStringField(QSHAttachmentFields.EXT));

            qicAttachmentDD.save(newAtt);
        }
    }
}

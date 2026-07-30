package com.fti.qm.helpers;

import com.fti.qm.constants.*;
import com.fti.qm.constants.qualityInspectionCommand.QICFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchOrders;
import com.qcadoo.model.api.search.SearchRestrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class QICHelperService {
    @Autowired
    private DataDefinitionService dataDefinitionService;

    /**
     * Sinh số thứ tự tiếp theo cho QIC theo inspectionType
     */
    public int generateNextInspectionOrderNumberInt(final DataDefinition qicDD, final String inspectionType) {
        SearchCriteriaBuilder scb = qicDD.find();
        scb.add(SearchRestrictions.eq(QICFields.INSPECTION_TYPE, inspectionType));
        scb.addOrder(SearchOrders.desc(QICFields.INSPECTION_ORDER_NUMBER_INT));
        scb.setMaxResults(1);

        Entity lastQic = scb.uniqueResult();
        if (lastQic == null) {
            return 1;
        }

        Integer lastNumber = lastQic.getIntegerField(QICFields.INSPECTION_ORDER_NUMBER_INT);
        return (lastNumber == null) ? 1 : lastNumber + 1;
    }

    /**
     * Kiểm tra xem sản phẩm có Tiêu chuẩn H tương ứng hay không
     */
    public boolean hasQualityStandardH(final Entity product, final String inspectionType) {
        DataDefinition hDD = dataDefinitionService.get(QMConstants.PLUGIN_IDENTIFIER, QMConstants.MODEL_QUALITY_STANDARD_H);
        if (hDD == null || product == null) {
            return false;
        }

        return !hDD.find()
                .add(SearchRestrictions.eq(GlobalFields.PRODUCT_ID, product.getId()))
                .add(SearchRestrictions.eq(QSHFields.TYPE, inspectionType))
                .add(SearchRestrictions.eq(GlobalFields.DELETED, false))
                .add(SearchRestrictions.eq(GlobalFields.ACTIVE, true))
                .setMaxResults(1)
                .list()
                .getEntities()
                .isEmpty();
    }

    /**
     * Tạo danh sách mẫu thử (Samples) dựa trên Tiêu chuẩn H và L
     */
    public void createSamples(final Entity qic, final Entity product, final String inspectionType) {
        DataDefinition sampleDD = dataDefinitionService.get(QMConstants.PLUGIN_IDENTIFIER, QMConstants.MODEL_QUALITY_STANDARD_SAMPLE);
        DataDefinition hDD = dataDefinitionService.get(QMConstants.PLUGIN_IDENTIFIER, QMConstants.MODEL_QUALITY_STANDARD_H);
        DataDefinition lDD = dataDefinitionService.get(QMConstants.PLUGIN_IDENTIFIER, QMConstants.MODEL_QUALITY_STANDARD_L);

        if (sampleDD == null || hDD == null || lDD == null || product == null || qic == null) {
            return;
        }

        List<Entity> hList = hDD.find()
                .add(SearchRestrictions.eq(GlobalFields.PRODUCT_ID, product.getId()))
                .add(SearchRestrictions.eq(QSHFields.TYPE, inspectionType))
                .add(SearchRestrictions.eq(GlobalFields.DELETED, false))
                .add(SearchRestrictions.eq(GlobalFields.ACTIVE, true))
                .list().getEntities();

        if (hList.isEmpty()) {
            return;
        }

        List<Long> hIds = hList.stream().map(Entity::getId).collect(Collectors.toList());

        List<Entity> lList = lDD.find()
                .add(SearchRestrictions.in(QSLFields.QUALITY_STANDARD_H_ID, hIds))
                .add(SearchRestrictions.eq(GlobalFields.DELETED, false))
                .list().getEntities();

        for (Entity l : lList) {
            Integer sampleSize = l.getIntegerField(QSLFields.SAMPLE_SIZE);
            if (sampleSize == null || sampleSize <= 0) {
                sampleSize = 1;
            }

            for (int i = 1; i <= sampleSize; i++) {
                Entity sample = sampleDD.create();
                sample.setField(QMConstants.MODEL_QUALITY_INSPECTION_COMMAND, qic);
                sample.setField(QMConstants.MODEL_QUALITY_STANDARD_L, l);
                sample.setField(QualityStandardSampleFields.SAMPLE_NUMBER, i);
                sampleDD.save(sample);
            }
        }
    }
}

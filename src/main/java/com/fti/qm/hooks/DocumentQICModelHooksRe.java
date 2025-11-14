package com.fti.qm.hooks;

import com.fti.qm.constants.QMConstants;
import com.fti.qm.constants.qualityInspectionCommand.QICFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchOrders;
import com.qcadoo.model.api.search.SearchProjections;
import com.qcadoo.model.api.search.SearchRestrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.qcadoo.mes.materialFlowResources.constants.DocumentFields;
import com.qcadoo.mes.deliveries.constants.DeliveredProductFields;
import com.qcadoo.mes.deliveries.constants.DeliveryFields;

import java.util.List;

@Service
public class DocumentQICModelHooksRe {
    public static final String FIELD_DELIVERY = "delivery";
    public static final String TYPE_RECEIPT = "01receipt";
    public static final String STATE_ACCEPTED = "02accepted";
    public static final String INSPECTION_ORDER_NUMBER_PREFIX_I = "I";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void createQualityInspectionCommandIfNeeded(final DataDefinition documentDD, final Entity document) {
        String type = document.getStringField(DocumentFields.TYPE);
        String state = document.getStringField(DocumentFields.STATE);
        Entity deliveryEntity = document.getBelongsToField(FIELD_DELIVERY);

        // --- NẾU LÀ EDIT THÌ BỎ QUA - Chỉ xử lý khi là phiếu nhập, đã được chấp nhận, và có delivery
        if (document.getId() != null || !TYPE_RECEIPT.equals(type) || !STATE_ACCEPTED.equals(state) || deliveryEntity == null) {
            return;
        }

        // --- Lấy tất cả DeliveredProducts từ Delivery
        List<Entity> deliveredProducts = deliveryEntity.getHasManyField(DeliveryFields.DELIVERED_PRODUCTS);
        if (deliveredProducts == null || deliveredProducts.isEmpty()) {
            return;
        }

        // --- Chuẩn bị DataDefinition cho QualityInspectionCommand
        DataDefinition qicDD = dataDefinitionService.get(QMConstants.PLUGIN_IDENTIFIER,QMConstants.MODEL_QUALITY_INSPECTION_COMMAND);
        if (qicDD == null) {
            return;
        }

        // --- Lặp qua từng deliveredProduct và tạo bản ghi QualityInspectionCommand
        for (Entity deliveredProduct : deliveredProducts) {
            Entity product = deliveredProduct.getBelongsToField(DeliveredProductFields.PRODUCT);
            if (product == null) {
                continue;
            }

            int nextNumber = generateNextInspectionOrderNumberInt(qicDD, QICFields.INSPECTION_TYPE_INCOMING);

            Entity qic = qicDD.create();
            qic.setField(QICFields.INSPECTION_TYPE, QICFields.INSPECTION_TYPE_INCOMING);
            qic.setField(QICFields.STATUS, QICFields.STATUS_NEW);
            qic.setField(QICFields.COMPANY, document.getBelongsToField(DocumentFields.COMPANY));
            qic.setField(QICFields.PRODUCT, product);
            qic.setField(QICFields.INSPECTION_ORDER_NUMBER, INSPECTION_ORDER_NUMBER_PREFIX_I);
            qic.setField(QICFields.INSPECTION_ORDER_NUMBER_INT, nextNumber);
            qic.setField(QICFields.PO_NUMBER, deliveryEntity.getStringField(DeliveryFields.NUMBER));
            qic.setField(QICFields.EXECUTION_DATE, document.getDateField(DocumentFields.TIME));
            qic.setField(QICFields.TRANSACTION_QUANTITY, deliveredProduct.getDecimalField(DeliveredProductFields.DELIVERED_QUANTITY));
            qic.setField(QICFields.LOCATION, deliveryEntity.getBelongsToField(DeliveryFields.LOCATION));

            qicDD.save(qic);
        }
    }

    private int generateNextInspectionOrderNumberInt(final DataDefinition qicDD, final String inspectionType) {
        SearchCriteriaBuilder scb = qicDD.find();
        scb.add(SearchRestrictions.eq(QICFields.INSPECTION_TYPE, inspectionType));
        scb.addOrder(SearchOrders.desc(QICFields.INSPECTION_ORDER_NUMBER_INT));
        scb.setMaxResults(1);

        Entity lastQic = scb.uniqueResult();
        if (lastQic == null) {
            return 1;
        }

        Integer lastNumber = lastQic.getIntegerField(QICFields.INSPECTION_ORDER_NUMBER_INT);
        if (lastNumber == null) {
            return 1;
        }

        return lastNumber + 1;
    }

}

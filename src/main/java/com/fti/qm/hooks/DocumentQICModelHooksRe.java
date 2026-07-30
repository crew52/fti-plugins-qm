package com.fti.qm.hooks;

import com.fti.qm.constants.*;
import com.fti.qm.constants.qualityInspectionCommand.QICFields;
import com.fti.qm.helpers.QICHelperService;
import com.fti.qm.services.QualityStandardAttachmentService;
import com.qcadoo.mes.deliveries.constants.DeliveredProductFields;
import com.qcadoo.mes.deliveries.constants.DeliveryFields;
import com.qcadoo.mes.materialFlowResources.constants.DocumentFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * Hook class để tạo tự động các bản ghi Quality Inspection Command (QIC) khi
 * một phiếu nhập (receipt) được tạo và đã được chấp nhận.
 */
@Service
public class DocumentQICModelHooksRe {
    public static final String FIELD_DELIVERY = "delivery";
    public static final String TYPE_RECEIPT = "01receipt";
    public static final String STATE_ACCEPTED = "02accepted";
    public static final String INSPECTION_ORDER_NUMBER_PREFIX_I = "IQC";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private QICHelperService qicHelperService;

    @Autowired
    private QualityStandardAttachmentService attachmentService;

    /**
     * Tạo các Quality Inspection Command (QIC) nếu cần dựa trên phiếu nhập.
     *
     * @param documentDD DataDefinition của document (phiếu nhập)
     * @param document   Entity của document hiện tại
     */
    public void createQualityInspectionCommandIfNeeded(final DataDefinition documentDD, final Entity document) {
        String type = document.getStringField(DocumentFields.TYPE);
        String state = document.getStringField(DocumentFields.STATE);
        Entity deliveryEntity = document.getBelongsToField(FIELD_DELIVERY);
        Entity locationTo = document.getBelongsToField(DocumentFields.LOCATION_TO);

        // --- NẾU LÀ EDIT THÌ BỎ QUA - Chỉ xử lý khi là phiếu nhập, đã được chấp nhận, và có delivery, Location To = QC
        if (document.getId() != null
                || !TYPE_RECEIPT.equals(type)
                || !STATE_ACCEPTED.equals(state)
                || deliveryEntity == null
                || locationTo == null
                || !"QC".equals(locationTo.getStringField("number"))) {
            return;
        }

        // --- Lấy tất cả DeliveredProducts từ Delivery
        List<Entity> deliveredProducts = deliveryEntity.getHasManyField(DeliveryFields.DELIVERED_PRODUCTS);
        if (deliveredProducts == null || deliveredProducts.isEmpty()) {
            return;
        }

        // --- Chuẩn bị DataDefinition cho QualityInspectionCommand
        DataDefinition qicDD = dataDefinitionService.get(QMConstants.PLUGIN_IDENTIFIER, QMConstants.MODEL_QUALITY_INSPECTION_COMMAND);
        if (qicDD == null) {
            return;
        }

        // --- Lặp qua từng deliveredProduct và tạo bản ghi QualityInspectionCommand
        for (Entity deliveredProduct : deliveredProducts) {
            Entity product = deliveredProduct.getBelongsToField(DeliveredProductFields.PRODUCT);
            if (product == null) {
                continue;
            }

            int nextNumber = qicHelperService.generateNextInspectionOrderNumberInt(qicDD, QICFields.InspectionType.INCOMING);

            Entity qic = qicDD.create();
            qic.setField(QICFields.INSPECTION_TYPE, QICFields.InspectionType.INCOMING);
            qic.setField(QICFields.STATUS, QICFields.Status.NEW);
            qic.setField(QICFields.COMPANY, document.getBelongsToField(DocumentFields.COMPANY));
            qic.setField(QICFields.PRODUCT, product);
            qic.setField(QICFields.INSPECTION_ORDER_NUMBER, INSPECTION_ORDER_NUMBER_PREFIX_I);
            qic.setField(QICFields.INSPECTION_ORDER_NUMBER_INT, nextNumber);
            qic.setField(QICFields.PO_NUMBER, deliveryEntity.getStringField(DeliveryFields.NUMBER));
            qic.setField(QICFields.EXECUTION_DATE, document.getDateField(DocumentFields.TIME));
            qic.setField(QICFields.TRANSACTION_QUANTITY, deliveredProduct.getDecimalField(DeliveredProductFields.DELIVERED_QUANTITY));
            qic.setField(QICFields.LOCATION, deliveryEntity.getBelongsToField(DeliveryFields.LOCATION));
            qic.setField(QICFields.VERSION, 1);
            qic.setField(QICFields.CREATED_DATE, new Date());

            qic = qicDD.save(qic);

            // --- Kiểm tra xem Product của QIC có Standard H nào không và xử lý sample / attachment
            if (qicHelperService.hasQualityStandardH(product, QICFields.InspectionType.INCOMING)) {
                try {
                    qicHelperService.createSamples(qic, product, QICFields.InspectionType.INCOMING);
                    attachmentService.copyAttachments(qic, product, QICFields.InspectionType.INCOMING, true);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
}
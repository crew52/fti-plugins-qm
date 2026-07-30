package com.fti.qm.hooks;

import com.fti.qm.constants.*;
import com.fti.qm.constants.qualityInspectionCommand.QICFields;
import com.fti.qm.helpers.QICHelperService;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
public class ProTrackingQICModelHook {

    public static final String INSPECTION_ORDER_NUMBER_PREFIX_P = "PQC";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private QICHelperService qicHelperService;

    /**
     * Tạo QIC (PQC) cho bản ghi ProductionTracking khi state = 02accepted & lastTracking = true
     */
    public void onUpdate(final DataDefinition productionTrackingDD, final Entity productionTracking) {
        if (productionTracking == null) {
            return;
        }

        // Lấy thông tin state và lastTracking
        String state = productionTracking.getStringField("state");
        Boolean lastTracking = productionTracking.getBooleanField("lastTracking");

        // Kiểm tra điều kiện: state = '02accepted' và lastTracking = true
        if ("02accepted".equals(state) && Boolean.TRUE.equals(lastTracking)) {
            createQICForProductionTracking(productionTracking);
        }
    }

    public void createQICForProductionTracking(final Entity productionTracking) {
        if (productionTracking == null) {
            return;
        }

        // 1. Kiểm tra Lệnh sản xuất (Order)
        Entity order = productionTracking.getBelongsToField("order");
        if (Objects.isNull(order)) {
            return;
        }

        // -----------------------------------------------------------------
        // KIỂM TRA BỎ QUA CÔNG ĐOẠN CUỐI CÙNG (Dành cho OQC đảm nhận)
        // -----------------------------------------------------------------
        Entity technologyOperationComponent = productionTracking.getBelongsToField("technologyOperationComponent");
        if (technologyOperationComponent != null && isLastOperationComponent(order, technologyOperationComponent)) {
            // Nếu là công đoạn cuối cùng, thoát ra không tạo PQC
            return;
        }

        // 2. Lấy danh sách sản phẩm đầu ra thực tế từ Entity gốc (trackingOperationProductOutComponents)
        List<Entity> trackingOutProducts = productionTracking.getHasManyField("trackingOperationProductOutComponents");

        if (trackingOutProducts == null || trackingOutProducts.isEmpty()) {
            return;
        }

        // Lấy sản phẩm đầu tiên từ đợt báo cáo sản xuất
        Entity trackingOutProductComp = trackingOutProducts.get(0);
        Entity product = trackingOutProductComp.getBelongsToField("product");

        if (product == null) {
            return;
        }

        // 2b. Lấy số lượng thực tế
        BigDecimal usedQuantity = trackingOutProductComp.getDecimalField("usedQuantity");

        // 3. Lấy vị trí kho (productsInputLocation) VÀ Mã công đoạn (operationNumber)
        Entity productsInputLocation = null;
        String operationNumber = null;

        if (technologyOperationComponent != null) {
            // --- 3a. Lấy Mã công đoạn và Tên công đoạn (operationNumber) ---
            Entity operation = technologyOperationComponent.getBelongsToField("operation");
            if (operation != null) {
                String code = operation.getStringField("number");
                String name = operation.getStringField("name");

                if (code != null && name != null) {
                    operationNumber = code + " - " + name;
                } else if (code != null) {
                    operationNumber = code;
                } else if (name != null) {
                    operationNumber = name;
                }
            }

            // --- 3b. Lấy Vị trí kho (productsInputLocation) ---
            Entity division = technologyOperationComponent.getBelongsToField("division");
            if (division != null) {
                productsInputLocation = division.getBelongsToField("productsInputLocation");
            }
        }

        // 4. Chuẩn bị các DataDefinition cần thiết
        DataDefinition qicDD = dataDefinitionService.get(QMConstants.PLUGIN_IDENTIFIER, QMConstants.MODEL_QUALITY_INSPECTION_COMMAND);

        if (qicDD == null) {
            return;
        }

        String prodOrderNum = order.getStringField(OrderFields.NUMBER);

        // -----------------------------------------------------------------
        // 5. TIẾN HÀNH TẠO QIC CHO PQC (TYPE: 02inprocess)
        // -----------------------------------------------------------------
        int nextNumber = qicHelperService.generateNextInspectionOrderNumberInt(qicDD, QICFields.InspectionType.IN_PROCESS);

        Entity qic = qicDD.create();
        qic.setField(QICFields.INSPECTION_TYPE, QICFields.InspectionType.IN_PROCESS);
        qic.setField(QICFields.STATUS, QICFields.Status.NEW);
        qic.setField(QICFields.PRODUCTION_ORDER_NUMBER, prodOrderNum);

        // Gán mã công đoạn tìm được
        qic.setField(QICFields.OPERATION_NUMBER, operationNumber);

        // Gán sản phẩm và thời gian thực hiện
        qic.setField(QICFields.PRODUCT, product);
        qic.setField(QICFields.EXECUTION_DATE, new Date());

        // Gán địa điểm/kho nếu tìm thấy
        if (productsInputLocation != null) {
            qic.setField(QICFields.LOCATION, productsInputLocation);
        }

        qic.setField(QICFields.INSPECTION_ORDER_NUMBER, INSPECTION_ORDER_NUMBER_PREFIX_P);
        qic.setField(QICFields.INSPECTION_ORDER_NUMBER_INT, nextNumber);
        qic.setField(QICFields.TRANSACTION_QUANTITY, usedQuantity);
        qic.setField(QICFields.VERSION, 1);
        qic.setField(QICFields.CREATED_DATE, new Date());

        qic = qicDD.save(qic);

        if (qic.isValid()) {
            // -----------------------------------------------------------------
            // 6. KIỂM TRA TIÊU CHUẨN CHẤT LƯỢNG H VÀ TẠO SAMPLES CHO PQC
            // -----------------------------------------------------------------
            if (qicHelperService.hasQualityStandardH(product, QICFields.InspectionType.IN_PROCESS)) {
                try {
                    qicHelperService.createSamples(qic, product, QICFields.InspectionType.IN_PROCESS);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    /**
     * Kiểm tra xem công đoạn hiện tại có phải là công đoạn cuối cùng trong quy trình sản xuất hay không.
     */
    private boolean isLastOperationComponent(final Entity order, final Entity currentOpComp) {
        Entity technology = order.getBelongsToField(OrderFields.TECHNOLOGY);
        if (technology == null) {
            return false;
        }

        List<Entity> opComponents = technology.getTreeField("operationComponents");
        if (opComponents == null || opComponents.isEmpty()) {
            return false;
        }
        // Lấy phần tử cuối cùng trong cây công đoạn
        Entity lastOpComponent = opComponents.get(0);
        boolean isLast = Objects.equals(lastOpComponent.getId(), currentOpComp.getId());
        return isLast;
    }
}
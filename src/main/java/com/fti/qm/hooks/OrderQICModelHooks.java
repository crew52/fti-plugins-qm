package com.fti.qm.hooks;

import com.fti.qm.constants.*;
import com.fti.qm.constants.qualityInspectionCommand.QICFields;
import com.qcadoo.mes.basic.constants.WorkstationFields;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchOrders;
import com.qcadoo.model.api.search.SearchRestrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class OrderQICModelHooks {

    public static final String INSPECTION_ORDER_NUMBER_PREFIX_O = "OQC";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void onUpdate(final DataDefinition dataDefinition, final Entity order) {
        // 1. Lấy trạng thái hiện tại dưới CSDL (trước khi bản ghi mới được lưu)
        Entity oldOrder = dataDefinition.get(order.getId());

        if (oldOrder != null) {
            String oldState = oldOrder.getStringField(OrderFields.STATE);
            String newState = order.getStringField(OrderFields.STATE);

            // 2. Kiểm tra điều kiện chuyển trạng thái từ 03inProgress sang 04completed
            if ("03inProgress".equals(oldState) && "04completed".equals(newState)) {
                createOQCForOrderOutputs(order);
            }
        }
    }

    public void createOQCForOrderOutputs(final Entity order) {
        if (order == null) {
            return;
        }

        // 1. Lấy Quy trình công nghệ (Technology) gắn với Lệnh sản xuất
        Entity technology = order.getBelongsToField(OrderFields.TECHNOLOGY);
        if (Objects.isNull(technology)) {
            return;
        }

        // 2. Lấy danh sách toàn bộ các công đoạn (Operation Components)
        List<Entity> opComponents = technology.getTreeField("operationComponents");
        if (opComponents == null || opComponents.isEmpty()) {
            return;
        }

        // 3. Lấy CÔNG ĐOẠN ĐẦU TIÊN trong Quy trình công nghệ
        Entity firstOpComponent = opComponents.get(0);

        // 4. Lấy danh sách sản phẩm đầu ra của CÔNG ĐOẠN ĐẦU TIÊN
        List<Entity> outProducts = firstOpComponent.getHasManyField("operationProductOutComponents");
        if (outProducts == null || outProducts.isEmpty()) {
            return;
        }

        // 5. Lấy SẢN PHẨM ĐẦU TIÊN trong operationProductOutComponents
        Entity firstOutProductComp = outProducts.get(0);
        Entity prod = firstOutProductComp.getBelongsToField("product");

        if (prod == null) {
            return;
        }

        // -----------------------------------------------------------------
        // LẤY WORKSTATION -> DIVISION -> PRODUCTS INPUT LOCATION
        // -----------------------------------------------------------------
        Entity productsInputLocation = null;
        List<Entity> workstations = firstOpComponent.getHasManyField("workstations");

        if (workstations != null && !workstations.isEmpty()) {
            // Lấy workstation đầu tiên gán trong công đoạn
            Entity workstation = workstations.get(0);
            if (workstation != null) {
                Entity division = workstation.getBelongsToField(WorkstationFields.DIVISION);
                if (division != null) {
                    productsInputLocation = division.getBelongsToField("productsInputLocation");
                }
            }
        }

        // Dự phòng: Nếu workstation chưa gán division, lấy trực tiếp division của công đoạn (nếu có)
        if (productsInputLocation == null) {
            Entity opDivision = firstOpComponent.getBelongsToField("division");
            if (opDivision != null) {
                productsInputLocation = opDivision.getBelongsToField("productsInputLocation");
            }
        }

        // 6. Chuẩn bị các DataDefinition cần thiết
        DataDefinition qicDD = dataDefinitionService.get(QMConstants.PLUGIN_IDENTIFIER, QMConstants.MODEL_QUALITY_INSPECTION_COMMAND);
        DataDefinition hDD = dataDefinitionService.get(QMConstants.PLUGIN_IDENTIFIER, QMConstants.MODEL_QUALITY_STANDARD_H);

        if (qicDD == null) {
            return;
        }

        String prodOrderNum = order.getStringField(OrderFields.NUMBER);
        BigDecimal plannedQty = order.getDecimalField(OrderFields.PLANNED_QUANTITY);

        // -----------------------------------------------------------------
        // TIẾN HÀNH TẠO 1 BẢN GHI QIC DUY NHẤT CHO OQC (TYPE: 03outgoing)
        // -----------------------------------------------------------------
        int nextNumber = generateNextInspectionOrderNumberInt(qicDD, QICFields.InspectionType.OUTGOING);

        Entity qic = qicDD.create();
        qic.setField(QICFields.INSPECTION_TYPE, QICFields.InspectionType.OUTGOING);
        qic.setField(QICFields.STATUS, QICFields.Status.NEW);
        qic.setField(QICFields.PRODUCTION_ORDER_NUMBER, prodOrderNum);

        // Gán sản phẩm đầu ra của công đoạn đầu tiên vào QIC
        qic.setField(QICFields.PRODUCT, prod);
        qic.setField(QICFields.EXECUTION_DATE, order.getDateField(OrderFields.START_DATE));

        // Gán vị trí kho đầu vào từ Division tìm được (nếu model QIC có field "location")
        if (productsInputLocation != null) {
            qic.setField(QICFields.LOCATION, productsInputLocation);
        }

        qic.setField(QICFields.INSPECTION_ORDER_NUMBER, INSPECTION_ORDER_NUMBER_PREFIX_O);
        qic.setField(QICFields.INSPECTION_ORDER_NUMBER_INT, nextNumber);
        qic.setField(QICFields.TRANSACTION_QUANTITY, plannedQty);
        qic.setField(QICFields.VERSION, 1);
        qic.setField(QICFields.CREATED_DATE, new Date());

        qic = qicDD.save(qic);

        if (qic.isValid()) {
            // -----------------------------------------------------------------
            // KIỂM TRA TIÊU CHUẨN CHẤT LƯỢNG H CHO SẢN PHẨM NÀY (TYPE: 03outgoing)
            // -----------------------------------------------------------------
            if (hDD != null) {
                boolean productHasStandardH = !hDD.find()
                        .add(SearchRestrictions.eq(GlobalFields.PRODUCT_ID, prod.getId()))
                        .add(SearchRestrictions.eq(QSHFields.TYPE, QICFields.InspectionType.OUTGOING))
                        .add(SearchRestrictions.eq(GlobalFields.DELETED, false))
                        .add(SearchRestrictions.eq(GlobalFields.ACTIVE, true))
                        .setMaxResults(1)
                        .list()
                        .getEntities()
                        .isEmpty();

                if (productHasStandardH) {
                    try {
                        createSamples(qic, prod, QICFields.InspectionType.OUTGOING);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
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
        return (lastNumber == null) ? 1 : lastNumber + 1;
    }

    private void createSamples(Entity qic, Entity product, String type) {

        DataDefinition sampleDD =
                dataDefinitionService.get(QMConstants.PLUGIN_IDENTIFIER, QMConstants.MODEL_QUALITY_STANDARD_SAMPLE);

        DataDefinition hDD =
                dataDefinitionService.get(QMConstants.PLUGIN_IDENTIFIER, QMConstants.MODEL_QUALITY_STANDARD_H);

        List<Entity> hList = hDD.find()
                .add(SearchRestrictions.eq(GlobalFields.PRODUCT_ID, product.getId()))
                .add(SearchRestrictions.eq(QSHFields.TYPE, type))
                .add(SearchRestrictions.eq(GlobalFields.DELETED, false))
                .add(SearchRestrictions.eq(GlobalFields.ACTIVE, true))
                .list().getEntities();

        if (hList.isEmpty()) return;

        List<Long> hIds = hList.stream().map(Entity::getId).collect(Collectors.toList());

        DataDefinition lDD =
                dataDefinitionService.get(QMConstants.PLUGIN_IDENTIFIER, QMConstants.MODEL_QUALITY_STANDARD_L);

        List<Entity> lList = lDD.find()
                .add(SearchRestrictions.in(QSLFields.QUALITY_STANDARD_H_ID, hIds))
                .add(SearchRestrictions.eq(GlobalFields.DELETED, false))
                .list().getEntities();

        for (Entity l : lList) {

            Integer sampleSize = l.getIntegerField(QSLFields.SAMPLE_SIZE);
            if (sampleSize == null || sampleSize <= 0) sampleSize = 1;

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

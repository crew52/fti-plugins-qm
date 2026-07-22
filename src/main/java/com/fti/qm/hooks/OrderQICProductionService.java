package com.fti.qm.hooks;
import com.fti.qm.constants.GlobalFields;
import com.fti.qm.constants.QMConstants;
import com.fti.qm.constants.QSHFields;
import com.fti.qm.constants.qualityInspectionCommand.QICFields;
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

/**
 * Service chịu trách nhiệm tự động sinh Quality Inspection Command (QIC)
 * cho các sản phẩm đầu ra của Production Order.
 *
 * <p>
 * Luồng xử lý:
 * <ol>
 *     <li>Nhận Production Order.</li>
 *     <li>Lấy Technology của Production Order.</li>
 *     <li>Duyệt toàn bộ Operation Component trong Technology.</li>
 *     <li>Lấy tất cả Output Product của từng Operation.</li>
 *     <li>Tạo một QIC cho mỗi Output Product.</li>
 *     <li>Nếu Product có Quality Standard Header (QSH) tương ứng thì
 *         tự động sinh các Sample Inspection.</li>
 * </ol>
 *
 * <p>
 * Service này được gọi sau khi Production Order chuyển sang trạng thái
 * <b>In Progress</b>.
 *
 * @author FTI
 */
@Service
public class OrderQICProductionService {
    public static final String INSPECTION_ORDER_NUMBER_PREFIX_P = "PQC";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    /**
     * Tạo Quality Inspection Command (QIC) cho toàn bộ sản phẩm đầu ra
     * của Production Order.
     *
     * <p>
     * Quy trình:
     * <ul>
     *     <li>Lấy Technology của Production Order.</li>
     *     <li>Duyệt toàn bộ Operation Component.</li>
     *     <li>Lấy Output Product của từng Operation.</li>
     *     <li>Tạo một QIC cho mỗi Product.</li>
     *     <li>Nếu Product có Quality Standard Header thì sinh Sample Inspection.</li>
     * </ul>
     *
     * <p>
     * Nếu Production Order không có Technology hoặc dữ liệu không hợp lệ,
     * phương thức sẽ kết thúc mà không thực hiện tạo QIC.
     *
     * @param order
     *      Production Order cần tạo Quality Inspection Command.
     */
    public void createQICForOrderOutputs(final Entity order) {
        if (order == null) {
            return;
        }

        // 1. Lấy Quy trình công nghệ (Technology) gắn với Lệnh sản xuất
        Entity technology = order.getBelongsToField("technology");
        if (Objects.isNull(technology)) {
            return;
        }

        // 2. Chuẩn bị các DataDefinition cần thiết
        DataDefinition qicDD = dataDefinitionService.get(QMConstants.PLUGIN_IDENTIFIER, QMConstants.MODEL_QUALITY_INSPECTION_COMMAND);
        DataDefinition hDD = dataDefinitionService.get(QMConstants.PLUGIN_IDENTIFIER, QMConstants.MODEL_QUALITY_STANDARD_H);

        if (qicDD == null) {
            return;
        }

        // 3. Quét cây công đoạn và sinh QIC cho TỪNG sản phẩm đầu ra tìm thấy
        List<Entity> opComponents = technology.getTreeField("operationComponents");

        if (opComponents != null && !opComponents.isEmpty()) {
            String prodOrderNum = order.getStringField("number");
            BigDecimal plannedQty = order.getDecimalField("plannedQuantity");

            for (Entity opComponent : opComponents) {
                // Lấy danh sách sản phẩm đầu ra gắn với công đoạn này
                List<Entity> outProducts = opComponent.getHasManyField("operationProductOutComponents");

                if (outProducts != null && !outProducts.isEmpty()) {
                    for (Entity outProductComp : outProducts) {
                        Entity prod = outProductComp.getBelongsToField("product");

                        if (prod != null) {
                            Long prodId = prod.getId();

                            // -----------------------------------------------------------------
                            // TIẾN HÀNH TẠO QIC CHO SẢN PHẨM NÀY
                            // -----------------------------------------------------------------
                            int nextNumber = generateNextInspectionOrderNumberInt(qicDD, QSHFields.Type.INPROCESS);

                            Entity qic = qicDD.create();
                            qic.setField(QICFields.INSPECTION_TYPE, QSHFields.Type.INPROCESS);
                            qic.setField(QICFields.STATUS, QICFields.Status.NEW);
                            qic.setField(QICFields.PRODUCTION_ORDER_NUMBER, prodOrderNum);

                            // Gán sản phẩm đầu ra (Bán thành phẩm hoặc Thành phẩm chính) vào QIC
                            qic.setField(QICFields.PRODUCT, prod);
                            qic.setField(QICFields.EXECUTION_DATE, order.getDateField("startDate"));

                            qic.setField(QICFields.INSPECTION_ORDER_NUMBER, INSPECTION_ORDER_NUMBER_PREFIX_P);
                            qic.setField(QICFields.INSPECTION_ORDER_NUMBER_INT, nextNumber);
                            qic.setField(QICFields.TRANSACTION_QUANTITY, plannedQty);
                            qic.setField(QICFields.VERSION, 1);
                            qic.setField(QICFields.CREATED_DATE, new Date());

                            qic = qicDD.save(qic);

                            if (qic.isValid()) {
                                // -----------------------------------------------------------------
                                // KIỂM TRA TIÊU CHUẨN CHẤT LƯỢNG H CHO SẢN PHẨM NÀY
                                // -----------------------------------------------------------------
                                if (hDD != null) {
                                    boolean productHasStandardH = !hDD.find()
                                            .add(SearchRestrictions.eq(GlobalFields.PRODUCT_ID, prodId))
                                            .add(SearchRestrictions.eq(QSHFields.TYPE, QSHFields.Type.INPROCESS))
                                            .add(SearchRestrictions.eq(GlobalFields.DELETED, false))
                                            .add(SearchRestrictions.eq(GlobalFields.ACTIVE, true))
                                            .setMaxResults(1)
                                            .list()
                                            .getEntities()
                                            .isEmpty();

                                    if (productHasStandardH) {
                                        try {
                                            createSamples(qic, prod, QSHFields.Type.INPROCESS);
                                        } catch (Exception ex) {
                                            // Giữ lại stackTrace của lỗi ngoại lệ để dễ debug khi hệ thống lỗi sâu bên trong
                                            ex.printStackTrace();
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private int generateNextInspectionOrderNumberInt(final DataDefinition qicDD, final String inspectionType) {
        SearchCriteriaBuilder scb = qicDD.find();
        scb.add(SearchRestrictions.eq("inspectionType", inspectionType));
        scb.addOrder(SearchOrders.desc("inspectionOrderNumberInt"));
        scb.setMaxResults(1);

        Entity lastQic = scb.uniqueResult();
        if (lastQic == null) {
            return 1;
        }

        Integer lastNumber = lastQic.getIntegerField("inspectionOrderNumberInt");
        return (lastNumber == null) ? 1 : lastNumber + 1;
    }

    private void createSamples(Entity qic, Entity product, String type) {

        DataDefinition sampleDD =
                dataDefinitionService.get(QMConstants.PLUGIN_IDENTIFIER, "qualityStandardSampleRe");

        DataDefinition hDD =
                dataDefinitionService.get(QMConstants.PLUGIN_IDENTIFIER, "qualityStandardH");

        List<Entity> hList = hDD.find()
                .add(SearchRestrictions.eq("product.id", product.getId()))
                .add(SearchRestrictions.eq("type", type))
                .add(SearchRestrictions.eq("deleted", false))
                .add(SearchRestrictions.eq("active", true))
                .list().getEntities();

        if (hList.isEmpty()) return;

        List<Long> hIds = hList.stream().map(Entity::getId).collect(Collectors.toList());

        DataDefinition lDD =
                dataDefinitionService.get(QMConstants.PLUGIN_IDENTIFIER, "qualityStandardL");

        List<Entity> lList = lDD.find()
                .add(SearchRestrictions.in("qualityStandardH.id", hIds))
                .add(SearchRestrictions.eq("deleted", false))
                .list().getEntities();

        for (Entity l : lList) {

            Integer sampleSize = l.getIntegerField("sampleSize");
            if (sampleSize == null || sampleSize <= 0) sampleSize = 1;

            for (int i = 1; i <= sampleSize; i++) {
                Entity sample = sampleDD.create();
                sample.setField("qualityInspectionCommandRe", qic);
                sample.setField("qualityStandardL", l);
                sample.setField("sampleNumber", i);
                sampleDD.save(sample);
            }
        }
    }
}

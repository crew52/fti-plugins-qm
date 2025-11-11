package com.fti.qm.hooks;

import com.fti.qm.constants.GlobalFields;
import com.fti.qm.constants.IncomingQualityStandardSampleFields;
import com.fti.qm.constants.QMConstants;
import com.fti.qm.constants.qualityInspectionCommand.QICFields;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.security.api.SecurityService;
import com.qcadoo.security.constants.QcadooSecurityConstants;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.Ribbon;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Hook class xử lý logic hiển thị và chuẩn bị dữ liệu cho màn hình chi tiết
 * của phiếu kiểm tra chất lượng (Quality Inspection Command - QIC).
 *
 * <p>Class này được gọi qua hook "beforeRender" trong XML view, chịu trách nhiệm:
 * <ul>
 *   <li>Kiểm tra loại kiểm tra và sản phẩm để đảm bảo tồn tại tiêu chuẩn kiểm tra tương ứng</li>
 *   <li>Tự động tạo mẫu thử (sample) nếu chưa có</li>
 *   <li>Tự động điền các thông tin phụ trợ như tên công ty, tên sản phẩm, tên dụng cụ, người dùng hiện tại, ngày kiểm tra</li>
 *   <li>Cập nhật trạng thái ban đầu của phiếu từ "NEW" sang "IN_PROGRESS"</li>
 *   <li>Vô hiệu hóa các hành động trong ribbon nếu thiếu tiêu chuẩn kiểm tra</li>
 * </ul>
 */
@Service
public class QICDetailsHooks {

    /**
     * Bản đồ ánh xạ giữa loại kiểm tra (inspectionType)
     * và tên model tiêu chuẩn tương ứng trong module QM.
     *
     * Ví dụ:
     * 01 (incoming) → incomingQualityStandardH
     * 02 (in-process) → inProcessQualityStandardH
     * 03 (outgoing) → outgoingQualityStandardH
     * 04 (equipment) → equipmentQualityStandardH
     */
    private static final Map<String, String> INSPECTION_TYPE_MODEL_MAP;
    static {
        Map<String, String> map = new HashMap<>();
        map.put(QICFields.INSPECTION_TYPE_INCOMING, QMConstants.MODEL_INCOMING_QUALITY_STANDARD_H);
        map.put(QICFields.INSPECTION_TYPE_IN_PROCESS, QMConstants.MODEL_IN_PROCESS_QUALITY_STANDARD_H);
        map.put(QICFields.INSPECTION_TYPE_OUTGOING, QMConstants.MODEL_OUTGOING_QUALITY_STANDARD_H);
        map.put(QICFields.INSPECTION_TYPE_EQUIPMENT, QMConstants.MODEL_EQUIPMENT_QUALITY_STANDARD_H);
        INSPECTION_TYPE_MODEL_MAP = Collections.unmodifiableMap(map);
    }

    @Autowired
    private SecurityService securityService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    /**
     * Hook chính, được gọi khi view QIC details được render.
     * <p>Thực hiện:
     * <ol>
     *   <li>Kiểm tra tiêu chuẩn theo loại kiểm tra và sản phẩm</li>
     *   <li>Tự động gán tên các trường thuộc (company, product, tool)</li>
     *   <li>Điền người dùng hiện tại vào field `user`</li>
     *   <li>Cập nhật trạng thái phiếu</li>
     *   <li>Tự động điền ngày kiểm tra nếu trống</li>
     * </ol>
     */
    public void beforeRender(final ViewDefinitionState view) {
        checkProduct(view);
        fillNameFromBelongsTo(view, QICFields.COMPANY, "companyName");
        fillNameFromBelongsTo(view, QICFields.PRODUCT, "productName");
        fillNameFromBelongsTo(view, QICFields.TOOL, "toolName");
        fillCurrentUser(view);
        updateStatusDisplay(view);
        fillCurrentInspectionDate(view);
    }

    /**
     * Kiểm tra sản phẩm có tiêu chuẩn kiểm tra tương ứng hay không.
     * <ul>
     *   <li>Nếu không có tiêu chuẩn: hiển thị cảnh báo và khóa các nút trong ribbon</li>
     *   <li>Nếu có: tạo mẫu thử (samples) nếu chưa tồn tại</li>
     * </ul>
     */
    private void checkProduct(final ViewDefinitionState view) {
        Entity qic = getFormEntity(view);
        if (qic == null) return;

        String inspectionType = qic.getStringField(QICFields.INSPECTION_TYPE);
        Entity product = qic.getBelongsToField(QICFields.PRODUCT);
        if (inspectionType == null || product == null) return;

        String modelName = INSPECTION_TYPE_MODEL_MAP.get(inspectionType);
        if (modelName == null) return;

        if (!checkIfStandardExists(modelName, product)) {
            view.addMessage("qm.qualityInspectionCommand.error.noStandardForProduct",
                    ComponentState.MessageType.INFO, false, product.getStringField(ProductFields.NUMBER));
            disableRibbonActionsExceptNavigation(view);
        }

        createSamplesIfNotExist(qic, modelName);
    }

    /**
     * Tự động tạo các mẫu thử (sample) cho từng dòng tiêu chuẩn L tương ứng,
     * nếu chưa có bản ghi trong bảng mẫu thử.
     *
     * @param qic        Entity phiếu kiểm tra chất lượng hiện tại
     * @param modelNameH Tên model của bảng tiêu chuẩn Header (ví dụ: incomingQualityStandardH)
     */
    private void createSamplesIfNotExist(Entity qic, String modelNameH) {
        DataDefinition sampleDD = dataDefinitionService.get(QMConstants.PLUGIN_IDENTIFIER, QMConstants.MODEL_INCOMING_QUALITY_STANDARD_SAMPLE);
        Entity product = qic.getBelongsToField(QICFields.PRODUCT);

        if (product == null) return;

        // 1️⃣ Lấy tất cả H theo product
        DataDefinition hDD = dataDefinitionService.get(QMConstants.PLUGIN_IDENTIFIER, modelNameH);
        List<Entity> hList = hDD.find()
                .add(SearchRestrictions.eq(GlobalFields.PRODUCT_ID, product.getId()))
                .add(SearchRestrictions.eq(GlobalFields.DELETED, false))
                .add(SearchRestrictions.eq(GlobalFields.ACTIVE, true))
                .list().getEntities();
        if (hList.isEmpty()) return;

        List<Long> hIds = hList.stream()
                .map(Entity::getId)
                .collect(Collectors.toList());

        // 2️⃣ Lấy tất cả L dựa trên H list
        String modelNameL = modelNameH.replace("H", "L");
        DataDefinition lDD = dataDefinitionService.get(QMConstants.PLUGIN_IDENTIFIER, modelNameL);
        List<Entity> lList = lDD.find()
                .add(SearchRestrictions.in(QMConstants.MODEL_INCOMING_QUALITY_STANDARD_H + GlobalFields.DOT_ID, hIds))
                .add(SearchRestrictions.eq(GlobalFields.DELETED, false))
                .list().getEntities();

        // 3️⃣ Tạo sample cho từng L nếu chưa tồn tại
        for (Entity l : lList) {
            List<Entity> existingSamplesForL = sampleDD.find()
                    .add(SearchRestrictions.eq(QMConstants.MODEL_QUALITY_INSPECTION_COMMAND + GlobalFields.DOT_ID, qic.getId()))
                    .add(SearchRestrictions.eq(QMConstants.MODEL_INCOMING_QUALITY_STANDARD_L + GlobalFields.DOT_ID, l.getId()))
                    .list().getEntities();

            if (!existingSamplesForL.isEmpty()) continue; // đã có sample cho L này → skip

            Integer sampleSize = l.getIntegerField("sampleSize");
            if (sampleSize == null || sampleSize <= 0) sampleSize = 1;

            for (int i = 1; i <= sampleSize; i++) {
                Entity sample = sampleDD.create();
                sample.setField(IncomingQualityStandardSampleFields.QUALITY_INSPECTION_COMMAND, qic);
                sample.setField(IncomingQualityStandardSampleFields.INCOMING_QUALITY_STANDARD_L, l);
                sample.setField(IncomingQualityStandardSampleFields.SAMPLE_NUMBER, i);
                sampleDD.save(sample);
            }
        }
    }

    /**
     * Lấy entity chính (form entity) từ view hiện tại.
     *
     * @param view ViewDefinitionState hiện tại
     * @return Entity hoặc null nếu không có form
     */
    private Entity getFormEntity(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        return (form != null) ? form.getEntity() : null;
    }

    /**
     * Kiểm tra xem tiêu chuẩn kiểm tra có tồn tại cho sản phẩm hiện tại hay không.
     */
    private boolean checkIfStandardExists(final String modelName, final Entity product) {
        DataDefinition dd = dataDefinitionService.get(QMConstants.PLUGIN_IDENTIFIER, modelName);
        return dd.find()
                .add(SearchRestrictions.eq("product.id", product.getId()))
                .add(SearchRestrictions.eq(GlobalFields.DELETED, false))
                .add(SearchRestrictions.eq(GlobalFields.ACTIVE, true))
                .setMaxResults(1)
                .uniqueResult() != null;
    }

    /**
     * Vô hiệu hóa tất cả các nút trong ribbon trừ nhóm "navigation"
     * → Dùng khi sản phẩm chưa có tiêu chuẩn kiểm tra.
     */
    private void disableRibbonActionsExceptNavigation(final ViewDefinitionState view) {
        WindowComponent window = (WindowComponent) view.getComponentByReference(QcadooViewConstants.L_WINDOW);
        if (window == null) {
            return;
        }

        Ribbon ribbon = window.getRibbon();
        if (ribbon == null) {
            return;
        }

        for (RibbonGroup group : ribbon.getGroups()) {
            if ("navigation".equals(group.getName())) {
                continue;
            }

            for (RibbonActionItem item : group.getItems()) {
                item.setEnabled(false);
                item.requestUpdate(true);
            }
        }
    }

    /**
     * Gán giá trị "name" của entity thuộc (BelongsTo) vào component input tương ứng.
     *
     * @param view         ViewDefinitionState hiện tại
     * @param belongsToRef Tên field belongsTo trong entity (vd: "company", "product", "tool")
     * @param targetRef    Tên component input trong view (vd: "companyName", "productName", "toolName")
     */
    private void fillNameFromBelongsTo(final ViewDefinitionState view, final String belongsToRef, final String targetRef) {
        Entity entity = getFormEntity(view);
        if (entity == null) return;

        Entity related = entity.getBelongsToField(belongsToRef);
        if (related == null) return;

        Optional.ofNullable((FieldComponent) view.getComponentByReference(targetRef))
                .ifPresent(field -> field.setFieldValue(related.getStringField("name")));
    }

    /**
     * Tự động điền người dùng hiện tại vào field "user" trong phiếu kiểm tra.
     */
    private void fillCurrentUser(final ViewDefinitionState view) {
        LookupComponent userLookup = (LookupComponent) view.getComponentByReference(QICFields.USER);
        if (userLookup == null) return;

        Long currentUserId = securityService.getCurrentUserId();
        if (currentUserId == null) return;

        // Lấy DataDefinition cho model "user"
        DataDefinition userDD = dataDefinitionService.get(QcadooSecurityConstants.PLUGIN_IDENTIFIER, QcadooSecurityConstants.MODEL_USER);

        // Lấy entity user hiện tại từ DB
        Entity currentUserEntity = userDD.get(currentUserId);
        if (currentUserEntity != null) {
            userLookup.setFieldValue(currentUserEntity.getId());
            userLookup.requestComponentUpdateState();
        }
    }

    /**
     * Nếu phiếu đang ở trạng thái "NEW" thì tự động cập nhật thành "IN_PROGRESS"
     * khi người dùng mở form.
     */
    private void updateStatusDisplay(final ViewDefinitionState view) {
        FieldComponent statusField = (FieldComponent) view.getComponentByReference(QICFields.STATUS);
        if (statusField == null) return;

        String currentStatus = (String) statusField.getFieldValue();

        if (QICFields.STATUS_NEW.equals(currentStatus)) {
            statusField.setFieldValue(QICFields.STATUS_IN_PROGRESS);
            statusField.requestComponentUpdateState();
        }
    }

    /**
     * Nếu chưa có ngày kiểm tra thì tự động set ngày hiện tại (yyyy-MM-dd).
     */
    private void fillCurrentInspectionDate(final ViewDefinitionState view) {
        FieldComponent inspectionDateField = (FieldComponent) view.getComponentByReference(QICFields.INSPECTION_DATE);
        if (inspectionDateField == null) return;

        // Kiểm tra giá trị hiện tại
        Object currentValue = inspectionDateField.getFieldValue();

        // Nếu chưa có giá trị thì set ngày hiện tại
        if (currentValue == null || currentValue.toString().isEmpty()) {
            LocalDate today = LocalDate.now(ZoneId.systemDefault());
            String todayStr = today.format(DateTimeFormatter.ISO_LOCAL_DATE); // "yyyy-MM-dd"
            inspectionDateField.setFieldValue(todayStr);
            inspectionDateField.requestComponentUpdateState();
        }
    }
}


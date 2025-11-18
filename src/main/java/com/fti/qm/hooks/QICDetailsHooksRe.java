package com.fti.qm.hooks;

import com.fti.qm.constants.*;
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
public class QICDetailsHooksRe {

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

        // Vì đã gộp model, ta chỉ cần gọi qualityStandardH
        if (!checkIfStandardExists(product, inspectionType)) {
            view.addMessage("qm.qualityInspectionCommand.error.noStandardForProduct",
                    ComponentState.MessageType.INFO, false, product.getStringField(ProductFields.NUMBER));
            disableRibbonActionsExceptNavigation(view);
            return;
        }

        createSamplesIfNotExist(qic, product, inspectionType);

        copyAttachmentsFromStandardH(qic, product, inspectionType);
    }

    private void copyAttachmentsFromStandardH(Entity qic, Entity product, String inspectionType) {

        DataDefinition hDD = dataDefinitionService.get(QMConstants.PLUGIN_IDENTIFIER, QMConstants.MODEL_QUALITY_STANDARD_H);

        // 1. Lấy danh sách qualityStandardH phù hợp
        List<Entity> hList = hDD.find()
                .add(SearchRestrictions.eq(GlobalFields.PRODUCT_ID, product.getId()))
                .add(SearchRestrictions.eq(QSHFields.TYPE, inspectionType))
                .add(SearchRestrictions.eq(GlobalFields.DELETED, false))
                .add(SearchRestrictions.eq(GlobalFields.ACTIVE, true))
                .list().getEntities();

        if (hList.isEmpty()) return;

        List<Long> hIds = hList.stream().map(Entity::getId).collect(Collectors.toList());

        // 2. Lấy tất cả attachment của StandardH
        DataDefinition stdAttachmentDD = dataDefinitionService.get(QMConstants.PLUGIN_IDENTIFIER, QMConstants.MODEL_QSH_ATTACHMENT);

        List<Entity> stdAttList = stdAttachmentDD.find()
                .add(SearchRestrictions.in("qualityStandardH.id", hIds))
                .add(SearchRestrictions.eq("deleted", false))
                .list().getEntities();

        if (stdAttList.isEmpty()) return;

        // 3. DataDefinition cho qicAttachment
        DataDefinition qicAttachmentDD = dataDefinitionService.get(QMConstants.PLUGIN_IDENTIFIER, QMConstants.MODEL_QIC_ATTACHMENT);

        // 4. Thực hiện copy từng attachment
        for (Entity stdAtt : stdAttList) {

            // Check tồn tại: tránh insert trùng
            boolean exists = !qicAttachmentDD.find()
                    .add(SearchRestrictions.eq("qualityInspectionCommandRe.id", qic.getId()))
//                    .add(SearchRestrictions.eq("sourceAttachmentId", stdAtt.getId())) // cột đánh dấu nguồn
                    .add(SearchRestrictions.eq("attachment", stdAtt.getStringField("attachment")))
                    .list().getEntities().isEmpty();

            if (exists) continue;

            // 5. Tạo mới attachment record cho QIC
            Entity newAtt = qicAttachmentDD.create();

            newAtt.setField("qualityInspectionCommandRe", qic);
            newAtt.setField("sourceAttachmentId", stdAtt.getId());
            newAtt.setField("attachment", stdAtt.getStringField("attachment"));
            newAtt.setField("name", stdAtt.getStringField("name"));
            newAtt.setField("size", stdAtt.getField("size"));
            newAtt.setField("ext", stdAtt.getStringField("ext"));

            qicAttachmentDD.save(newAtt);
        }
    }

    private void createSamplesIfNotExist(Entity qic, Entity product, String type) {

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

            boolean exists = !sampleDD.find()
                    .add(SearchRestrictions.eq(QualityStandardSampleFields.QUALITY_INSPECTION_COMMAND_ID, qic.getId()))
                    .add(SearchRestrictions.eq(QualityStandardSampleFields.QUALITY_STANDARD_L_ID, l.getId()))
                    .list().getEntities().isEmpty();

            if (exists) continue;

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
    private boolean checkIfStandardExists(final Entity product, final String type) {
        DataDefinition dd = dataDefinitionService.get(QMConstants.PLUGIN_IDENTIFIER, QMConstants.MODEL_QUALITY_STANDARD_H);

        return dd.find()
                .add(SearchRestrictions.eq(GlobalFields.PRODUCT_ID, product.getId()))
                .add(SearchRestrictions.eq(QSHFields.TYPE, type))
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


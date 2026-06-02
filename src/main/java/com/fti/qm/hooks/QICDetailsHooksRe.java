package com.fti.qm.hooks;

import com.fti.qm.constants.*;
import com.fti.qm.constants.qualityInspectionCommand.QICFields;
import com.fti.qm.utils.RibbonUtils;
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
import com.qcadoo.view.api.components.lookup.FilterValueHolder;
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
        fillInspectionOrderDisplay(view);
        fillCurrentUser(view);
        updateStatusDisplay(view);
        fillCurrentInspectionDate(view);

        setupLocationFilters(view);

        disableFieldsIfCompleted(view);
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
            RibbonUtils.disableActionsExceptNavigation(view);
            return;
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

        if (QICFields.Status.NEW.equals(currentStatus)) {
            statusField.setFieldValue(QICFields.Status.IN_PROGRESS);
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

    /**
     * Thiết lập filter cho các lookup kho trên tab Quality Decision.
     *
     * <p>Filter sẽ:
     * <ul>
     *     <li>Loại trừ location hiện tại của QIC khỏi danh sách chọn.</li>
     *     <li>Truyền qualityDecision hiện tại từ UI vào criteria modifier.</li>
     * </ul>
     * </p>
     *
     * @param view trạng thái view hiện tại
     */
    private void setupLocationFilters(ViewDefinitionState view) {

        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        if (form == null) return;

        Long qicId = form.getEntityId();
        if (qicId == null) return;

        DataDefinition qicDD = dataDefinitionService.get(QMConstants.PLUGIN_IDENTIFIER, QMConstants.MODEL_QUALITY_INSPECTION_COMMAND);
        Entity qic = qicDD.get(qicId);
        if (qic == null) return;

        Entity location = qic.getBelongsToField(QICFields.LOCATION);
        if (location == null) {
            return;
        }

        Long locationId = location.getId();

        FieldComponent qualityDecision =
                (FieldComponent) view.getComponentByReference(
                        QICFields.QUALITY_DECISION);

        String decision =
                qualityDecision.getFieldValue() != null
                        ? qualityDecision.getFieldValue().toString()
                        : null;

        applyFilter(view, QICFields.WAREHOUSE_LOCATION, locationId, decision);
        applyFilter(view, QICFields.NG_LOCATION, locationId, decision);
    }

    /**
     * Gán giá trị filter cho lookup location.
     *
     * <p>Filter bao gồm:
     * <ul>
     *     <li>excludedLocationId: ID location cần loại trừ.</li>
     *     <li>qualityDecision: quyết định chất lượng hiện tại trên màn hình.</li>
     * </ul>
     * </p>
     *
     * @param view view hiện tại
     * @param lookupName tên lookup cần áp dụng filter
     * @param excludedId ID location cần loại trừ
     * @param decision quality decision hiện tại
     */
    private void applyFilter(ViewDefinitionState view, String lookupName, Long excludedId, String decision) {

        LookupComponent lookup = (LookupComponent) view.getComponentByReference(lookupName);
        if (lookup == null) {
            return;
        }

        FilterValueHolder filter = lookup.getFilterValue();
        filter.put("excludedLocationId", excludedId);
        filter.put("qualityDecision", decision);
        lookup.setFilterValue(filter);
    }

    /**
     * Hiển thị lệnh kiểm tra (Inspection Order) trong view.
     * <p>
     * Method này kết hợp 2 field gốc từ database:
     * <ul>
     *     <li>{@code inspectionOrderNumber} (prefix, String)</li>
     *     <li>{@code inspectionOrderNumberInt} (số thứ tự, Integer)</li>
     * </ul>
     * Sau đó ghép chúng thành một chuỗi hiển thị và set vào field ảo
     * {@code inspectionOrder} trong view.
     * <p>
     * Lưu ý:
     * <ul>
     *     <li>Không lấy dữ liệu từ form, mà truy vấn trực tiếp từ database.</li>
     *     <li>Trường {@code inspectionOrder} chỉ dùng để hiển thị, không lưu DB.</li>
     *     <li>Nếu prefix hoặc number là null, field hiển thị sẽ để rỗng.</li>
     * </ul>
     *
     * @param view trạng thái của view hiện tại, dùng để lấy form và field hiển thị
     */
    private void fillInspectionOrderDisplay(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        Long qicId = form.getEntityId();

        if (qicId == null) return;

        DataDefinition qicDD = dataDefinitionService.get(QMConstants.PLUGIN_IDENTIFIER, QMConstants.MODEL_QUALITY_INSPECTION_COMMAND);
        Entity qicFromDB = qicDD.get(qicId);

        String prefix = qicFromDB.getStringField(QICFields.INSPECTION_ORDER_NUMBER);
        Integer number = qicFromDB.getIntegerField(QICFields.INSPECTION_ORDER_NUMBER_INT);

        String display = "";
        if (prefix != null && number != null) {
            display = prefix + number;
        }

        FieldComponent inspectionOrderField =
                (FieldComponent) view.getComponentByReference("inspectionOrder");

        inspectionOrderField.setFieldValue(display);
        inspectionOrderField.requestComponentUpdateState();
    }

    /**
     * Disable các field chỉ định (khai báo ngay trong method)
     * khi status = COMPLETED.
     */
    private void disableFieldsIfCompleted(final ViewDefinitionState view) {

        // 1) Lấy status
        FieldComponent statusField =
                (FieldComponent) view.getComponentByReference(QICFields.STATUS);
        if (statusField == null) return;

        String status = (String) statusField.getFieldValue();

        // 2) Không phải COMPLETED → thoát
        if (!QICFields.Status.COMPLETED.equals(status)) {
            return;
        }

        // 3) Danh sách field muốn disable (bạn chỉnh sửa danh sách này)
        List<String> fieldsToDisable = Arrays.asList(
                "status",
                "user",
                "inspectionDate",
                "note",
                "qualityDecision",
                "qualityDecisionCheckBox",
                "warehouseQuantity",
                "ngQuantity",
                "warehouseLocation",
                "ngLocation"
        );

        // 4) Disable từng field trong danh sách
        for (String fieldRef : fieldsToDisable) {
            ComponentState comp = view.getComponentByReference(fieldRef);
            if (comp != null) {
                comp.setEnabled(false);
            }
        }
        RibbonUtils.disableActionsExceptNavigation(view);
    }
}


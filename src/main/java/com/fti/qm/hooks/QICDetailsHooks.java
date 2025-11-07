package com.fti.qm.hooks;

import com.fti.qm.constants.qualityInspectionCommand.QICFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
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

import java.util.HashMap;
import java.util.Map;

@Service
public class QICDetailsHooks {

    @Autowired
    private SecurityService securityService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void beforeRenderCheckProduct(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference("form");
        if (form == null || form.getEntity() == null) {
            return;
        }

        Entity qic = form.getEntity();
        String inspectionType = qic.getStringField("inspectionType");

        // --- Nếu inspectionType chưa được chọn, bỏ qua
        if (inspectionType == null) {
            return;
        }

        // --- Lấy product liên kết
        Entity product = qic.getBelongsToField("product");
        if (product == null) {
            return;
        }

        // --- Xác định model tiêu chuẩn tương ứng theo loại inspection
        String modelName = getStandardModelName(inspectionType);
        if (modelName == null) {
            return;
        }

        // --- Kiểm tra tồn tại bản ghi tiêu chuẩn
        boolean hasStandard = checkIfStandardExists(modelName, product);

        if (!hasStandard) {
            String productNumber = product.getStringField("number");

            view.addMessage("qm.qualityInspectionCommand.error.noStandardForProduct",
                    ComponentState.MessageType.INFO, false, productNumber);

            form.setVisible(false);
            disableRibbonActionsExceptNavigation(view);
        }
    }

    /**
     * Xác định model tiêu chuẩn tương ứng với loại kiểm tra
     */
    private String getStandardModelName(final String inspectionType) {
        Map<String, String> typeToModel = new HashMap<>();
        typeToModel.put("01incoming", "incomingQualityStandardH");
        typeToModel.put("02inprocess", "inProcessQualityStandardH");
        typeToModel.put("03outgoing", "outgoingQualityStandardH");
        typeToModel.put("04equipment", "equipmentQualityStandardH");
        return typeToModel.get(inspectionType);
    }

    /**
     * Kiểm tra xem có bản ghi tiêu chuẩn tồn tại cho product không
     */
    private boolean checkIfStandardExists(final String modelName, final Entity product) {
        DataDefinition standardDD = dataDefinitionService.get("qm", modelName);
        SearchCriteriaBuilder scb = standardDD.find()
                .add(SearchRestrictions.eq("product.id", product.getId()))
                .add(SearchRestrictions.eq("deleted", false))
                .add(SearchRestrictions.eq("active", true));

        return scb.list().getTotalNumberOfEntities() > 0;
    }

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

    public void beforeRender(final ViewDefinitionState view) {
        fillNameFromBelongsTo(view, QICFields.COMPANY, "companyName");
        fillNameFromBelongsTo(view, QICFields.PRODUCT, "productName");
        fillNameFromBelongsTo(view, QICFields.TOOL, "toolName");
        fillCurrentUser(view);
        updateStatusDisplay(view);
    }

    /**
     * Hàm dùng chung: Lấy entity từ field belongsTo và gán giá trị name vào component tương ứng
     *
     * @param view         ViewDefinitionState hiện tại
     * @param belongsToRef Tên field belongsTo trong entity (vd: "company", "product", "tool")
     * @param targetRef    Tên component input trong view (vd: "companyName", "productName", "toolName")
     */
    private void fillNameFromBelongsTo(final ViewDefinitionState view, final String belongsToRef, final String targetRef) {
        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        if (form == null || form.getEntity() == null) {
            return;
        }

        Entity entity = form.getEntity();
        Entity relatedEntity = entity.getBelongsToField(belongsToRef);
        if (relatedEntity == null) {
            return;
        }

        String nameValue = relatedEntity.getStringField("name");
        if (nameValue == null) {
            return;
        }

        ComponentState targetInput = view.getComponentByReference(targetRef);
        if (targetInput != null) {
            targetInput.setFieldValue(nameValue);
        }
    }

    private void fillCurrentUser(final ViewDefinitionState view) {
        LookupComponent userLookup = (LookupComponent) view.getComponentByReference(QICFields.USER);
        if (userLookup == null) {
            return;
        }

        Long currentUserId = securityService.getCurrentUserId();
        if (currentUserId == null) {
            return;
        }

        // Lấy DataDefinition cho model "user"
        DataDefinition userDD = dataDefinitionService.get(QcadooSecurityConstants.PLUGIN_IDENTIFIER, QcadooSecurityConstants.MODEL_USER);

        // Lấy entity user hiện tại từ DB
        Entity currentUserEntity = userDD.get(currentUserId);
        if (currentUserEntity != null) {
            userLookup.setFieldValue(currentUserEntity.getId());
        }
    }

    private void updateStatusDisplay(final ViewDefinitionState view) {
        FieldComponent statusField = (FieldComponent) view.getComponentByReference(QICFields.STATUS);

        if (statusField == null) {
            return;
        }

        String currentStatus = (String) statusField.getFieldValue();

        if (QICFields.STATUS_NEW.equals(currentStatus)) {
            statusField.setFieldValue(QICFields.STATUS_IN_PROGRESS);
            statusField.requestComponentUpdateState();
        }
    }
}


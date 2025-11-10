package com.fti.qm.hooks;

import com.fti.qm.constants.GlobalFields;
import com.fti.qm.constants.QMConstants;
import com.fti.qm.constants.qualityInspectionCommand.QICFields;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.search.SearchResult;
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
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class QICDetailsHooks {

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

    public void beforeRender(final ViewDefinitionState view) {
        // Kiểm tra product & chuẩn
        checkProduct(view);
        // Gán tên company, product, tool
        fillNameFromBelongsTo(view, QICFields.COMPANY, "companyName");
        fillNameFromBelongsTo(view, QICFields.PRODUCT, "productName");
        fillNameFromBelongsTo(view, QICFields.TOOL, "toolName");
        // Điền user hiện tại
        fillCurrentUser(view);
        // Cập nhật trạng thái
        updateStatusDisplay(view);

        // Điền ngày kiểm tra hiện tại
        fillCurrentInspectionDate(view);
    }

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
    private void createSamplesIfNotExist(Entity qic, String modelNameH) {
        DataDefinition sampleDD = dataDefinitionService.get("qm", "incomingQualityStandardSample");
        Entity product = qic.getBelongsToField(QICFields.PRODUCT);

        if (product == null) return;

        // 1️⃣ Kiểm tra xem đã có sample chưa
        List<Entity> existingSamples = sampleDD.find()
                .add(SearchRestrictions.eq("qualityInspectionCommand.id", qic.getId()))
                .list().getEntities();
        if (!existingSamples.isEmpty()) return;

        // 2️⃣ Lấy tất cả H theo product
        DataDefinition hDD = dataDefinitionService.get("qm", modelNameH);
        List<Entity> hList = hDD.find()
                .add(SearchRestrictions.eq("product.id", product.getId()))
                .add(SearchRestrictions.eq(GlobalFields.DELETED, false))
                .add(SearchRestrictions.eq(GlobalFields.ACTIVE, true))
                .list().getEntities();
        if (hList.isEmpty()) return;

        List<Long> hIds = hList.stream()
                .map(Entity::getId)
                .collect(Collectors.toList());

        // 3️⃣ Lấy tất cả L dựa trên H list
        String modelNameL = modelNameH.replace("H", "L");
        DataDefinition lDD = dataDefinitionService.get("qm", modelNameL);
        List<Entity> lList = lDD.find()
                .add(SearchRestrictions.in("incomingQualityStandardH.id", hIds))
                .add(SearchRestrictions.eq(GlobalFields.DELETED, false))
//                .add(SearchRestrictions.eq(GlobalFields.ACTIVE, true))
                .list().getEntities();

        // 4️⃣ Tạo sample dựa trên L
        for (Entity l : lList) {
            Integer sampleSize = l.getIntegerField("sampleSize");
            if (sampleSize == null || sampleSize <= 0) sampleSize = 1;

            for (int i = 1; i <= sampleSize; i++) {
                Entity sample = sampleDD.create();
                sample.setField("qualityInspectionCommand", qic);
                sample.setField("incomingQualityStandardL", l);
                sample.setField("sampleNumber", i);
                sampleDD.save(sample);
            }
        }
    }

//    private void createSamplesIfNotExist(Entity qic, String modelNameH) {
//        DataDefinition sampleDD = dataDefinitionService.get("qm", "incomingQualityStandardSample");
//        Entity product = qic.getBelongsToField(QICFields.PRODUCT);
//
//        if (product == null) return;
//
//        // 1️⃣ Lấy tất cả H theo product
//        DataDefinition hDD = dataDefinitionService.get("qm", modelNameH);
//        List<Entity> hList = hDD.find()
//                .add(SearchRestrictions.eq("product.id", product.getId()))
//                .add(SearchRestrictions.eq(GlobalFields.DELETED, false))
//                .add(SearchRestrictions.eq(GlobalFields.ACTIVE, true))
//                .list().getEntities();
//        if (hList.isEmpty()) return;
//
//        List<Long> hIds = hList.stream()
//                .map(Entity::getId)
//                .collect(Collectors.toList());
//
//        // 2️⃣ Lấy tất cả L dựa trên H list
//        String modelNameL = modelNameH.replace("H", "L");
//        DataDefinition lDD = dataDefinitionService.get("qm", modelNameL);
//        List<Entity> lList = lDD.find()
//                .add(SearchRestrictions.in("incomingQualityStandardH.id", hIds))
//                .add(SearchRestrictions.eq(GlobalFields.DELETED, false))
//                .list().getEntities();
//
//        // 3️⃣ Tạo sample cho từng L nếu chưa tồn tại
//        for (Entity l : lList) {
//            List<Entity> existingSamplesForL = sampleDD.find()
//                    .add(SearchRestrictions.eq("qualityInspectionCommand.id", qic.getId()))
//                    .add(SearchRestrictions.eq("incomingQualityStandardL.id", l.getId()))
//                    .list().getEntities();
//
//            if (!existingSamplesForL.isEmpty()) continue; // đã có sample cho L này → skip
//
//            Integer sampleSize = l.getIntegerField("sampleSize");
//            if (sampleSize == null || sampleSize <= 0) sampleSize = 1;
//
//            for (int i = 1; i <= sampleSize; i++) {
//                Entity sample = sampleDD.create();
//                sample.setField("qualityInspectionCommand", qic);
//                sample.setField("incomingQualityStandardL", l);
//                sample.setField("sampleNumber", i);
//                sampleDD.save(sample);
//            }
//        }
//    }



    private Entity getFormEntity(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        return (form != null) ? form.getEntity() : null;
    }

    /**
     * Kiểm tra xem có bản ghi tiêu chuẩn tồn tại cho product không
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
     * Hàm dùng chung: Lấy entity từ field belongsTo và gán giá trị name vào component tương ứng
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

    private void updateStatusDisplay(final ViewDefinitionState view) {
        FieldComponent statusField = (FieldComponent) view.getComponentByReference(QICFields.STATUS);
        if (statusField == null) return;

        String currentStatus = (String) statusField.getFieldValue();

        if (QICFields.STATUS_NEW.equals(currentStatus)) {
            statusField.setFieldValue(QICFields.STATUS_IN_PROGRESS);
            statusField.requestComponentUpdateState();
        }
    }

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


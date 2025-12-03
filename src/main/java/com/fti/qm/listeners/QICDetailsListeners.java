package com.fti.qm.listeners;

import com.fti.qm.constants.qualityInspectionCommand.QICFields;
import com.fti.qm.utils.DecimalFieldFormatter;
import com.fti.qm.utils.DecimalFieldListenerUtils;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class QICDetailsListeners {
    @Autowired
    private DecimalFieldFormatter decimalFieldFormatter;

    @Autowired
    private DecimalFieldListenerUtils decimalFieldListenerUtils;

    /**
     * Xử lý thay đổi trạng thái checkbox và quyết định chất lượng (qualityDecision).
     *
     * <p>Luồng xử lý:</p>
     * <ul>
     *   <li>Nếu checkbox = false → chỉ disable 4 field liên quan, không xóa dữ liệu.</li>
     *   <li>Nếu checkbox = true → so sánh quyết định hiện tại với quyết định gốc trong DB:
     *       <ul>
     *           <li>Giống quyết định gốc → giữ nguyên dữ liệu 4 field.</li>
     *           <li>Khác quyết định gốc → reset (set null) 4 field và enable lại theo loại quyết định.</li>
     *       </ul>
     *   </li>
     * </ul>
     *
     * <p>Quy tắc enable field theo quyết định:</p>
     * <ul>
     *   <li>ACCEPT → chỉ enable Warehouse quantity + location.</li>
     *   <li>REJECT → chỉ enable NG quantity + location.</li>
     *   <li>PARTIAL → enable cả 4 field.</li>
     * </ul>
     *
     * @param view            trạng thái view hiện tại
     * @param componentState  component phát sinh sự kiện
     * @param args            tham số sự kiện
     */
    public void onQualityDecisionCheckBoxChange(final ViewDefinitionState view,
                                                final ComponentState componentState,
                                                final String[] args) {

        FieldComponent checkBox = (FieldComponent) view.getComponentByReference(QICFields.QUALITY_DECISION_CHECKBOX);
        boolean isChecked = "1".equals(checkBox.getFieldValue());

        // Components
        FieldComponent warehouseQty = (FieldComponent) view.getComponentByReference(QICFields.WAREHOUSE_QUANTITY);
        LookupComponent warehouseLoc = (LookupComponent) view.getComponentByReference(QICFields.WAREHOUSE_LOCATION);
        FieldComponent ngQty = (FieldComponent) view.getComponentByReference(QICFields.NG_QUANTITY);
        LookupComponent ngLoc = (LookupComponent) view.getComponentByReference(QICFields.NG_LOCATION);
        FieldComponent decisionField = (FieldComponent) view.getComponentByReference(QICFields.QUALITY_DECISION);

        // Enable/disable qualityDecision theo checkbox
        decisionField.setEnabled(!isChecked);
        decisionField.requestComponentUpdateState();

        // Lấy entity từ form (entity hiện trên màn hình)
        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        Entity entity = form.getEntity();
        BigDecimal transactionQty = entity.getDecimalField(QICFields.TRANSACTION_QUANTITY);

        String decision = decisionField.getFieldValue() != null
                ? decisionField.getFieldValue().toString()
                : "";

        // ❗ Lấy dữ liệu gốc từ DB
        Entity entityFromDB = entity.getDataDefinition().get(entity.getId());
        String originalDecision = entityFromDB.getStringField(QICFields.QUALITY_DECISION);

        // Nếu checkbox bỏ chọn → reset y như cũ
        if (!isChecked) {
            disable(warehouseQty, warehouseLoc, ngQty, ngLoc);
            setRequiredFields(null, warehouseQty, warehouseLoc, ngQty, ngLoc);
            return;
        }

        // --- Từ đây checkbox = true ---

        boolean isSameDecision = decision.equals(originalDecision);

        // Nếu quyết định đang chọn KHÔNG phải quyết định gốc → reset 4 field
        if (!isSameDecision) {
            disable(warehouseQty, warehouseLoc, ngQty, ngLoc);
            warehouseQty.setFieldValue(null);
            warehouseLoc.setFieldValue(null);
            ngQty.setFieldValue(null);
            ngLoc.setFieldValue(null);
        }

        // ------------------- Switch logic -------------------
        switch (decision) {
            case QICFields.QualityDecision.ACCEPT:
                warehouseLoc.setEnabled(true);
                if (transactionQty != null && !isSameDecision) {
                    warehouseQty.setFieldValue(transactionQty.toString());
                }
                break;

            case QICFields.QualityDecision.REJECT:
                ngLoc.setEnabled(true);
                if (transactionQty != null && !isSameDecision) {
                    ngQty.setFieldValue(transactionQty.toString());
                }
                break;

            case QICFields.QualityDecision.PARTIAL:
                warehouseQty.setEnabled(true);
                warehouseLoc.setEnabled(true);
                ngQty.setEnabled(true);
                ngLoc.setEnabled(true);
                break;

            default:
                break;
        }

        setRequiredFields(decision, warehouseQty, warehouseLoc, ngQty, ngLoc);

        updateFields(warehouseQty, ngQty);
        updateLookups(warehouseLoc, ngLoc);
    }

    private void disable(FieldComponent warehouseQty,
                         LookupComponent warehouseLoc,
                         FieldComponent ngQty,
                         LookupComponent ngLoc) {

        warehouseQty.setEnabled(false);
        warehouseLoc.setEnabled(false);
        ngQty.setEnabled(false);
        ngLoc.setEnabled(false);
    }

    private void updateFields(FieldComponent... fields) {
        for (FieldComponent f : fields) {
            f.requestComponentUpdateState();
        }
    }

    private void updateLookups(LookupComponent... lookups) {
        for (LookupComponent l : lookups) {
            l.requestComponentUpdateState();
        }
    }

    public void onWarehouseQtyChange(final ViewDefinitionState view,
                                     final ComponentState state,
                                     final String[] args) {

        decimalFieldListenerUtils.handleDecimalInput(view, state);

        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        Entity entity = form.getEntity();
        BigDecimal transactionQty = entity.getDecimalField(QICFields.TRANSACTION_QUANTITY);
        if (transactionQty == null) {
            return;
        }

        FieldComponent warehouseQty = (FieldComponent) view.getComponentByReference(QICFields.WAREHOUSE_QUANTITY);
        FieldComponent ngQty = (FieldComponent) view.getComponentByReference(QICFields.NG_QUANTITY);

        // Lấy giá trị decimal đã normalize
        BigDecimal warehouseValue = decimalFieldFormatter.normalizeDecimalField(warehouseQty, false);
        if (warehouseValue == null) {
            warehouseValue = BigDecimal.ZERO;
        }

        // Tính NG = Transaction - Warehouse
        BigDecimal ngValue = transactionQty.subtract(warehouseValue);
        if (ngValue.compareTo(BigDecimal.ZERO) < 0) {
            ngValue = BigDecimal.ZERO;
        }

        // Cập nhật UI
        ngQty.setFieldValue(decimalFieldFormatter.formatDecimalForLocale(ngValue));

        ngQty.requestComponentUpdateState();
    }

    public void onNgQtyChange(final ViewDefinitionState view,
                              final ComponentState state,
                              final String[] args) {

        // Normalize giá trị vừa nhập
        decimalFieldListenerUtils.handleDecimalInput(view, state);

        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        Entity entity = form.getEntity();
        BigDecimal transactionQty = entity.getDecimalField(QICFields.TRANSACTION_QUANTITY);
        if (transactionQty == null) {
            return;
        }

        FieldComponent warehouseQty = (FieldComponent) view.getComponentByReference(QICFields.WAREHOUSE_QUANTITY);
        FieldComponent ngQty = (FieldComponent) view.getComponentByReference(QICFields.NG_QUANTITY);

        // Lấy giá trị decimal sau khi normalize
        BigDecimal ngValue = decimalFieldFormatter.normalizeDecimalField(ngQty, false);
        if (ngValue == null) {
            ngValue = BigDecimal.ZERO;
        }

        // Tính Warehouse = Transaction - NG
        BigDecimal warehouseValue = transactionQty.subtract(ngValue);
        if (warehouseValue.compareTo(BigDecimal.ZERO) < 0) {
            warehouseValue = BigDecimal.ZERO;
        }

        warehouseQty.setFieldValue(decimalFieldFormatter.formatDecimalForLocale(warehouseValue));
        warehouseQty.requestComponentUpdateState();
    }

    private void setRequiredFields(String decision,
                                   FieldComponent warehouseQty,
                                   LookupComponent warehouseLoc,
                                   FieldComponent ngQty,
                                   LookupComponent ngLoc) {

        // reset required trước
        warehouseQty.setRequired(false);
        warehouseLoc.setRequired(false);
        ngQty.setRequired(false);
        ngLoc.setRequired(false);

        if (decision == null) {
            return;
        }

        switch (decision) {

            case QICFields.QualityDecision.ACCEPT:
                warehouseLoc.setRequired(true);
                break;

            case QICFields.QualityDecision.REJECT:
                ngLoc.setRequired(true);
                break;

            case QICFields.QualityDecision.PARTIAL:
                warehouseQty.setRequired(true);
                warehouseLoc.setRequired(true);
                ngQty.setRequired(true);
                ngLoc.setRequired(true);
                break;

            default:
                break;
        }

        // cần update UI
        warehouseQty.requestComponentUpdateState();
        warehouseLoc.requestComponentUpdateState();
        ngQty.requestComponentUpdateState();
        ngLoc.requestComponentUpdateState();
    }

}

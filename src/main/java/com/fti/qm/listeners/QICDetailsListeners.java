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

        if (!isChecked) {
            resetAndDisable(warehouseQty, warehouseLoc, ngQty, ngLoc);
            return;
        }

        // Lấy entity
        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        Entity entity = form.getEntity();
        BigDecimal transactionQty = entity.getDecimalField(QICFields.TRANSACTION_QUANTITY);

        String decision = decisionField.getFieldValue() != null
                ? decisionField.getFieldValue().toString()
                : "";

        if (decision.isEmpty()) {
            return;
        }

        // Reset và disable tất cả trước
        disable(warehouseQty, warehouseLoc, ngQty, ngLoc);
        warehouseQty.setFieldValue(null);
        warehouseLoc.setFieldValue(null);
        ngQty.setFieldValue(null);
        ngLoc.setFieldValue(null);

        // ---------------- Switch-case theo quyết định ----------------
        switch (decision) {
            case QICFields.QualityDecision.ACCEPT:
                warehouseLoc.setEnabled(true);
                if (transactionQty != null) {
                    warehouseQty.setFieldValue(transactionQty.toString());
                }
                break;

            case QICFields.QualityDecision.REJECT:
                ngLoc.setEnabled(true);
                if (transactionQty != null) {
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

        // Update UI
        updateFields(warehouseQty, ngQty);
        updateLookups(warehouseLoc, ngLoc);
    }

    private void resetAndDisable(FieldComponent warehouseQty,
                                 LookupComponent warehouseLoc,
                                 FieldComponent ngQty,
                                 LookupComponent ngLoc) {

        warehouseQty.setFieldValue(null);
        warehouseQty.setEnabled(false);

        warehouseLoc.setFieldValue(null);
        warehouseLoc.setEnabled(false);

        ngQty.setFieldValue(null);
        ngQty.setEnabled(false);

        ngLoc.setFieldValue(null);
        ngLoc.setEnabled(false);

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

}

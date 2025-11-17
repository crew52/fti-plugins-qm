package com.fti.qm.listeners;

import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class QICDetailsListeners {
    public void onQualityDecisionCheckBoxChange(final ViewDefinitionState view,
                                                final ComponentState componentState,
                                                final String[] args) {

        FieldComponent checkBox = (FieldComponent) view.getComponentByReference("qualityDecisionCheckBox");
        boolean isChecked = "1".equals(checkBox.getFieldValue());

        // Components
        FieldComponent warehouseQty = (FieldComponent) view.getComponentByReference("warehouseQuantity");
        LookupComponent warehouseLoc = (LookupComponent) view.getComponentByReference("warehouseLocation");
        FieldComponent ngQty = (FieldComponent) view.getComponentByReference("ngQuantity");
        LookupComponent ngLoc = (LookupComponent) view.getComponentByReference("ngLocation");
        FieldComponent decisionField = (FieldComponent) view.getComponentByReference("qualityDecision");

        // Enable/disable qualityDecision theo checkbox
        decisionField.setEnabled(!isChecked);
        decisionField.requestComponentUpdateState();

        if (!isChecked) {
            resetAndDisable(warehouseQty, warehouseLoc, ngQty, ngLoc);
            return;
        }

        // Lấy entity
        FormComponent form = (FormComponent) view.getComponentByReference("form");
        Entity entity = form.getEntity();
        BigDecimal transactionQty = entity.getDecimalField("transactionQuantity");

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
            case "01accept":
                warehouseLoc.setEnabled(true);
                if (transactionQty != null) {
                    warehouseQty.setFieldValue(transactionQty.toString());
                }
                break;

            case "02reject":
                ngLoc.setEnabled(true);
                if (transactionQty != null) {
                    ngQty.setFieldValue(transactionQty.toString());
                }
                break;

            case "03partial":
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

        FormComponent form = (FormComponent) view.getComponentByReference("form");
        Entity entity = form.getEntity();
        BigDecimal transactionQty = entity.getDecimalField("transactionQuantity");
        if (transactionQty == null) {
            return;
        }

        FieldComponent warehouseQty = (FieldComponent) view.getComponentByReference("warehouseQuantity");
        FieldComponent ngQty = (FieldComponent) view.getComponentByReference("ngQuantity");

        BigDecimal warehouseValue = safeParseDecimal(warehouseQty.getFieldValue());
        BigDecimal ngValue = transactionQty.subtract(warehouseValue);
        if (ngValue.compareTo(BigDecimal.ZERO) < 0) {
            ngValue = BigDecimal.ZERO;
        }

        ngQty.setFieldValue(ngValue);
        ngQty.requestComponentUpdateState();
    }

    public void onNgQtyChange(final ViewDefinitionState view,
                              final ComponentState state,
                              final String[] args) {

        FormComponent form = (FormComponent) view.getComponentByReference("form");
        Entity entity = form.getEntity();
        BigDecimal transactionQty = entity.getDecimalField("transactionQuantity");
        if (transactionQty == null) {
            return;
        }

        FieldComponent warehouseQty = (FieldComponent) view.getComponentByReference("warehouseQuantity");
        FieldComponent ngQty = (FieldComponent) view.getComponentByReference("ngQuantity");

        BigDecimal ngValue = safeParseDecimal(ngQty.getFieldValue());
        BigDecimal warehouseValue = transactionQty.subtract(ngValue);
        if (warehouseValue.compareTo(BigDecimal.ZERO) < 0) {
            warehouseValue = BigDecimal.ZERO;
        }

        warehouseQty.setFieldValue(warehouseValue);
        warehouseQty.requestComponentUpdateState();
    }

    /**
     * Chuyển Object sang BigDecimal an toàn.
     * Trả về BigDecimal.ZERO nếu null hoặc không hợp lệ.
     */
    private BigDecimal safeParseDecimal(Object value) {
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        } else if (value instanceof String) {
            String str = ((String) value).trim();
            if (!str.isEmpty()) {
                try {
                    return new BigDecimal(str);
                } catch (NumberFormatException e) {
                    // bỏ qua và trả về ZERO
                }
            }
        }
        return BigDecimal.ZERO;
    }

}

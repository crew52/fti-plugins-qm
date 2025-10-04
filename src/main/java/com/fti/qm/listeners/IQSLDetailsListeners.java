package com.fti.qm.listeners;

import com.fti.qm.constants.IncomingQualityStandardLFields;
import com.fti.qm.constants.QualityCriteriaFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.LookupComponent;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

@Service
public class IQSLDetailsListeners {
    public final void onQualityCriteriaChange(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        LookupComponent qualityCriteriaField = (LookupComponent) view.getComponentByReference(IncomingQualityStandardLFields.QUALITY_CRITERIA);

        if (qualityCriteriaField.getFieldValue() == null) {
            return;
        }

        Entity qualityCriteria = qualityCriteriaField.getEntity();
        if (qualityCriteria == null) {
            return;
        }

        FieldComponent unitField = (FieldComponent) view.getComponentByReference(IncomingQualityStandardLFields.UNIT);

        if (unitField.getFieldValue() != null && !"".equals(unitField.getFieldValue().toString())) {
            return;
        }

        Object unitValue = qualityCriteria.getField(QualityCriteriaFields.UNIT);
        if (unitValue != null) {
            unitField.setFieldValue(unitValue);
            unitField.requestComponentUpdateState();
        }
    }

    /**
     * Xử lý khi người dùng thay đổi giá trị của quantitativeValue hoặc tolerance.
     *
     * - Tính upValue = quantitativeValue + tolerance
     * - Tính downValue = quantitativeValue - tolerance
     * - Tự động định dạng lại tất cả các giá trị theo ngôn ngữ người dùng.
     *
     * Lưu ý: hỗ trợ định dạng dấu phẩy (,) hoặc dấu chấm (.) cho cả vi_VN và en_US.
     *
     * @param view trạng thái view hiện tại
     * @param state thành phần được thay đổi (component gọi event)
     * @param args tham số truyền thêm (nếu có)
     */
    public void onQuantitativeOrToleranceChange(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FieldComponent quantitativeField = (FieldComponent) view.getComponentByReference("quantitativeValue");
        FieldComponent toleranceField = (FieldComponent) view.getComponentByReference("tolerance");
        FieldComponent upValueField = (FieldComponent) view.getComponentByReference("upValue");
        FieldComponent downValueField = (FieldComponent) view.getComponentByReference("downValue");

        // Chuyển dấu phẩy sang dấu chấm cho parsing BigDecimal
        String qValStr = safeToDot((String) quantitativeField.getFieldValue());
        String tolStr = safeToDot((String) toleranceField.getFieldValue());

        if (qValStr == null || qValStr.isEmpty() || tolStr == null || tolStr.isEmpty()) {
            return;
        }

        try {
            BigDecimal qVal = new BigDecimal(qValStr);

            // Cập nhật lại quantitativeValue theo locale (3.5 -> 3,5 nếu vi_VN)
            quantitativeField.setFieldValue(formatDecimalForLocale(qVal));
            quantitativeField.requestComponentUpdateState();

            if (tolStr != null && !tolStr.isEmpty()) {
                BigDecimal tolVal = new BigDecimal(tolStr);

                // ✅ Cập nhật lại tolerance
                toleranceField.setFieldValue(formatDecimalForLocale(tolVal));
                toleranceField.requestComponentUpdateState();

                BigDecimal upVal = qVal.add(tolVal);
                BigDecimal downVal = qVal.subtract(tolVal);

                // ✅ Gán kết quả lên up/down theo locale
                upValueField.setFieldValue(formatDecimalForLocale(upVal));
                downValueField.setFieldValue(formatDecimalForLocale(downVal));
            } else {
                if (isEmpty(upValueField)) {
                    upValueField.setFieldValue(formatDecimalForLocale(qVal));
                }
                if (isEmpty(downValueField)) {
                    downValueField.setFieldValue(formatDecimalForLocale(qVal));
                }
            }

            upValueField.requestComponentUpdateState();
            downValueField.requestComponentUpdateState();

        } catch (NumberFormatException e) {
            // Không xử lý nếu không phải số
        }
    }

    /**
     * Kiểm tra một field có rỗng không (null hoặc chuỗi trắng).
     *
     * @param field component cần kiểm tra
     * @return true nếu rỗng, ngược lại false
     */
    private boolean isEmpty(FieldComponent field) {
        return field.getFieldValue() == null || field.getFieldValue().toString().trim().isEmpty();
    }

    /**
     * Hàm chuyển đổi dấu phẩy thành dấu chấm để đảm bảo BigDecimal có thể parse đúng.
     *
     * @param value giá trị đầu vào (có thể là "3,5" hoặc "3.5")
     * @return chuỗi đã thay , → .
     */
    private String safeToDot(String value) {
        if (value == null) {
            return null;
        }
        return value.replace(',', '.').trim();
    }

    /**
     * Format giá trị số về chuỗi đúng định dạng ngôn ngữ hiện tại (locale).
     *
     * - vi_VN → sử dụng dấu phẩy (,)
     * - en_US → sử dụng dấu chấm (.)
     *
     * @param value giá trị BigDecimal
     * @return chuỗi đã định dạng theo locale
     */
    private String formatDecimalForLocale(BigDecimal value) {
        if (value == null) return "";
        Locale locale = LocaleContextHolder.getLocale();
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(locale);
        DecimalFormat df = new DecimalFormat("#0.##", symbols);
        return df.format(value);
    }

}
